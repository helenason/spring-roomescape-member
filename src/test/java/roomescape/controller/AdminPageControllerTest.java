package roomescape.controller;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import roomescape.service.AuthService;
import roomescape.service.dto.AuthDto;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class AdminPageControllerTest {

    private final JdbcTemplate jdbcTemplate;
    private final AuthService authService;
    private final SimpleJdbcInsert memberInsertActor;

    @Autowired
    public AdminPageControllerTest(JdbcTemplate jdbcTemplate, AuthService authService) {
        this.jdbcTemplate = jdbcTemplate;
        this.authService = authService;
        this.memberInsertActor = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("member")
                .usingGeneratedKeyColumns("id");
    }

    @BeforeEach
    void setUp() {
        initDatabase();
        insertMember("에버", "treeboss@gmail.com", "treeboss123!", "USER");
        insertMember("관리자", "admin@gmail.com", "admin123!", "ADMIN");
    }

    private void initDatabase() {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.execute("TRUNCATE TABLE member RESTART IDENTITY");
    }

    private void insertMember(String name, String email, String password, String role) {
        Map<String, Object> parameters = new HashMap<>(4);
        parameters.put("name", name);
        parameters.put("email", email);
        parameters.put("password", password);
        parameters.put("role", role);
        memberInsertActor.execute(parameters);
    }

    @DisplayName("관리자가 어드민 페이지에 접속할 경우 예외를 반환하지 않는다.")
    @Test
    void should_throw_exception_when_admin_contact() {
        AuthDto authDto = new AuthDto("admin@gmail.com");
        String token = authService.createToken(authDto);
        RestAssured
                .given().log().all()
                .cookie("token", token)
                .when().get("/admin")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("일반 유저가 어드민 페이지에 접속할 경우 예외를 반환한다.")
    @Test
    void should_not_throw_exception_when_user_contact() {
        AuthDto authDto = new AuthDto("treeboss@gmail.com");
        String token = authService.createToken(authDto);
        RestAssured
                .given().log().all()
                .cookie("token", token)
                .when().get("/admin")
                .then().log().all()
                .statusCode(401);
    }
}