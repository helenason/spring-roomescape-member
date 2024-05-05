package roomescape.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.model.Reservation;
import roomescape.model.ReservationTime;
import roomescape.model.Theme;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ReservationRepositoryTest {

    private static final int INITIAL_RESERVATION_COUNT = 2;

    private final JdbcTemplate jdbcTemplate;
    private final ReservationRepository reservationRepository;
    private final SimpleJdbcInsert themeInsertActor;
    private final SimpleJdbcInsert timeInsertActor;
    private final SimpleJdbcInsert reservationInsertActor;

    @Autowired
    public ReservationRepositoryTest(JdbcTemplate jdbcTemplate, ReservationRepository reservationRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.reservationRepository = reservationRepository;
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
        insertTheme("n1", "d1", "t1");
        insertTheme("n2", "d2", "t2");
        insertReservationTime("1:00");
        insertReservationTime("2:00");
        insertReservation("n1", "2000-01-01", 1, 1);
        insertReservation("n2", "2000-01-02", 2, 2);
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

    private void insertReservation(String name, String date, long timeId, long themeId) {
        Map<String, Object> parameters = new HashMap<>(4);
        parameters.put("name", name);
        parameters.put("date", date);
        parameters.put("time_id", timeId);
        parameters.put("theme_id", themeId);
        reservationInsertActor.execute(parameters);
    }

    @DisplayName("모든 예약을 조회한다.")
    @Test
    void should_find_all_reservations() {
        List<Reservation> reservations = reservationRepository.findAllReservations();
        assertThat(reservations).hasSize(INITIAL_RESERVATION_COUNT);
    }

    @DisplayName("특정 id를 가진 예약 시간을 조회한다.")
    @Test
    void should_find_reservation_time_by_id() {
        Optional<ReservationTime> time = reservationRepository.findReservationTimeById(1);
        assertThat(time).hasValue(new ReservationTime(1, LocalTime.of(1, 0)));
    }

    @DisplayName("특정 id를 가진 테마를 조회한다.")
    @Test
    void should_find_theme_by_id() {
        Optional<Theme> time = reservationRepository.findThemeById(1);
        assertThat(time).hasValue(new Theme(1, "n1", "d1", "t1"));
    }

    @DisplayName("특정 날짜와 시간을 가진 예약이 존재하는 경우 참을 반환한다.")
    @Test
    void should_return_true_when_exist_reservation_by_date_and_timeId() {
        boolean isExist = reservationRepository.isExistReservationByDateAndTimeId(LocalDate.of(2000, 1, 1), 1);
        assertThat(isExist).isTrue();
    }

    @DisplayName("특정 날짜와 시간을 가진 예약이 존재하지 않는 경우 거짓을 반환한다.")
    @Test
    void should_return_false_when_not_exist_reservation_by_date_and_timeId() {
        boolean isExist = reservationRepository.isExistReservationByDateAndTimeId(LocalDate.of(9999, 1, 1), 1);
        assertThat(isExist).isFalse();
    }

    @DisplayName("예약을 저장한 후 저장된 예약을 반환한다.")
    @Test
    void should_save_reservation() {
        ReservationTime time = new ReservationTime(1, LocalTime.of(1, 0));
        Theme theme = new Theme(1, "n1", "d1", "t1");
        Reservation before = new Reservation("n3", LocalDate.of(2000, 1, 3), time, theme);
        Reservation actual = reservationRepository.saveReservation(before);

        Reservation after = new Reservation(3, "n3", LocalDate.of(2000, 1, 3), time, theme);
        assertThat(actual).isEqualTo(after);
    }

    @DisplayName("특정 id를 가진 예약이 존재하는 경우 참을 반환한다.")
    @Test
    void should_return_true_when_exist_reservation_by_id() {
        boolean isExist = reservationRepository.isExistReservationById(1);
        assertThat(isExist).isTrue();
    }

    @DisplayName("특정 id를 가진 예약이 존재하지 않는 경우 거짓을 반환한다.")
    @Test
    void should_return_false_when_not_exist_reservation_by_id() {
        boolean isExist = reservationRepository.isExistReservationById(999);
        assertThat(isExist).isFalse();
    }

    @DisplayName("예약을 삭제한다.")
    @Test
    void should_delete_reservation() {
        reservationRepository.deleteReservationById(1);
        assertThat(reservationRepository.findAllReservations()).hasSize(INITIAL_RESERVATION_COUNT - 1);
    }

    @DisplayName("특정 날짜와 테마의 예약된 시간을 조회한다.")
    @Test
    void should_find_booked_reservation_time() { // TODO: test case 구체화
        List<ReservationTime> bookedTimes = reservationRepository.findReservationTimeBooked(LocalDate.of(2000, 1, 1), 1);
        assertThat(bookedTimes).containsExactly(new ReservationTime(1, LocalTime.of(1, 0)));
    }

    @DisplayName("특정 날짜와 테마의 예약되지 않은 시간을 조회한다.")
    @Test
    void should_find_not_booked_reservation_time() { // TODO: test case 구체화
        List<ReservationTime> notBookedTimes = reservationRepository.findReservationTimeNotBooked(LocalDate.of(2000, 1, 1), 1);
        assertThat(notBookedTimes).containsExactly(new ReservationTime(2, LocalTime.of(2, 0)));
    }
}