package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.controller.request.ReservationRequest;
import roomescape.controller.response.MemberReservationTimeResponse;
import roomescape.controller.response.ReservationResponse;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ReservationControllerTest {

    private static final int INITIAL_TIME_COUNT = 5;
    private static final int INITIAL_RESERVATION_COUNT = 15;

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert themeInsertActor;
    private final SimpleJdbcInsert timeInsertActor;
    private final SimpleJdbcInsert reservationInsertActor;

    @Autowired
    public ReservationControllerTest(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.themeInsertActor = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");
        this.timeInsertActor = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
        this.reservationInsertActor = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    @BeforeEach
    void setUp() {
        initDatabase();
        IntStream.range(1, 6).forEach(i -> insertReservationTime(i + ":00"));
        IntStream.range(0, 20).forEach(i -> insertTheme("n" + i, "d" + i, "t" + i));

        LocalDate date = LocalDate.now().minusDays(1);
        IntStream.range(0, 1).forEach(i -> insertReservation("n" + i, date, 1, 1));
        IntStream.range(0, 2).forEach(i -> insertReservation("n" + i, date, 1, 2));
        IntStream.range(0, 3).forEach(i -> insertReservation("n" + i, date, 1, 3));
        IntStream.range(0, 4).forEach(i -> insertReservation("n" + i, date, 2, 1));
        IntStream.range(0, 5).forEach(i -> insertReservation("n" + i, date, 2, 2));
    }

    private void initDatabase() {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.execute("TRUNCATE TABLE theme RESTART IDENTITY");
        jdbcTemplate.execute("TRUNCATE TABLE reservation_time RESTART IDENTITY");
        jdbcTemplate.execute("TRUNCATE TABLE reservation RESTART IDENTITY");
    }

    private void insertTheme(String name, String description, String thumbnail) {
        Map<String, Object> parameters = new HashMap<>(3);
        parameters.put("name", name);
        parameters.put("description", description);
        parameters.put("thumbnail", thumbnail);
        themeInsertActor.execute(parameters);
    }

    private void insertReservationTime(String startAt) {
        Map<String, Object> parameters = new HashMap<>(1);
        parameters.put("start_at", startAt);
        timeInsertActor.execute(parameters);
    }

    private void insertReservation(String name, LocalDate date, long timeId, long themeId) {
        Map<String, Object> parameters = new HashMap<>(4);
        parameters.put("name", name);
        parameters.put("date", date);
        parameters.put("time_id", timeId);
        parameters.put("theme_id", themeId);
        reservationInsertActor.execute(parameters);
    }

    @DisplayName("전체 예약을 조회한다.")
    @Test
    void should_get_all_reservations() {
        List<ReservationResponse> reservations = RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath().getList(".", ReservationResponse.class);

        assertThat(reservations).hasSize(INITIAL_RESERVATION_COUNT);
    }

    @DisplayName("예약을 추가할 수 있다.")
    @Test
    void should_insert_reservation() {
        ReservationRequest request = new ReservationRequest("n", LocalDate.now().plusDays(1), 1L, 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .header("Location", "/reservations/" + (INITIAL_RESERVATION_COUNT + 1));

        assertThat(countAllReservations()).isEqualTo(INITIAL_RESERVATION_COUNT + 1);
    }

    @DisplayName("존재하는 예약이라면 예약을 삭제할 수 있다.")
    @Test
    void should_delete_reservation_when_reservation_exist() {
        RestAssured.given().log().all()
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(204);

        assertThat(countAllReservations()).isEqualTo(INITIAL_RESERVATION_COUNT - 1);
    }

    @DisplayName("특정 날짜와 테마에 따른 모든 시간의 예약 가능 여부를 확인한다.")
    @Test
    void should_get_reservations_with_book_state_by_date_and_theme() {
        String date = LocalDate.now().minusDays(1).toString(); // LD 으로 바꾸어보자!
        long themeId = 1;
        List<MemberReservationTimeResponse> times = RestAssured.given().log().all()
                .when().get(String.format("/reservations/times?date=%s&themeId=%d", date, themeId))
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath().getList(".", MemberReservationTimeResponse.class);
        System.out.println(times);
        assertThat(times).hasSize(INITIAL_TIME_COUNT);
        assertThat(times).containsOnly(
                new MemberReservationTimeResponse(1, LocalTime.of(1, 0), true),
                new MemberReservationTimeResponse(2, LocalTime.of(2, 0), true),
                new MemberReservationTimeResponse(3, LocalTime.of(3, 0), false),
                new MemberReservationTimeResponse(4, LocalTime.of(4, 0), false),
                new MemberReservationTimeResponse(5, LocalTime.of(5, 0), false)
        );
    }

    private Integer countAllReservations() {
        return jdbcTemplate.queryForObject("SELECT count(id) from reservation", Integer.class);
    }
}
