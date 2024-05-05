package roomescape.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.controller.response.MemberReservationTimeResponse;
import roomescape.exception.BadRequestException;
import roomescape.exception.DuplicatedException;
import roomescape.exception.NotFoundException;
import roomescape.model.Reservation;
import roomescape.model.ReservationTime;
import roomescape.model.Theme;
import roomescape.repository.ReservationRepository;
import roomescape.repository.dao.ReservationDao;
import roomescape.repository.dao.ReservationTimeDao;
import roomescape.repository.dao.ThemeDao;
import roomescape.repository.dto.ReservationSavedDto;
import roomescape.service.dto.ReservationDto;
import roomescape.service.fakedao.FakeReservationDao;
import roomescape.service.fakedao.FakeReservationTimeDao;
import roomescape.service.fakedao.FakeThemeDao;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class ReservationServiceTest {

    private static final int INITIAL_RESERVATION_COUNT = 3;
    private static final int INITIAL_TIME_COUNT = 3;

    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        ThemeDao themeDao = new FakeThemeDao(new ArrayList<>(List.of(
                new Theme(1, "n1", "d1", "t1"),
                new Theme(2, "n2", "d2", "t2"),
                new Theme(3, "n3", "d3", "t3"))));
        ReservationTimeDao reservationTimeDao = new FakeReservationTimeDao(new ArrayList<>(List.of(
                new ReservationTime(1, LocalTime.of(1, 0)),
                new ReservationTime(2, LocalTime.of(2, 0)),
                new ReservationTime(3, LocalTime.now()))));
        ReservationDao reservationDao = new FakeReservationDao(new ArrayList<>(List.of(
                new ReservationSavedDto(1, "n1", LocalDate.of(2000, 1, 1), 1L, 1L),
                new ReservationSavedDto(2, "n2", LocalDate.of(2000, 1, 2), 2L, 2L),
                new ReservationSavedDto(3, "n3", LocalDate.of(9999, 9, 9), 1L, 1L))));
        reservationService = new ReservationService(new ReservationRepository(reservationDao, reservationTimeDao, themeDao));
    }

    @DisplayName("모든 예약을 반환한다.")
    @Test
    void should_find_all_reservations() {
        List<Reservation> reservations = reservationService.findAllReservations();
        assertThat(reservations).hasSize(INITIAL_RESERVATION_COUNT);
    }

    @DisplayName("예약을 추가한다.")
    @Test
    void should_save_reservation() {
        ReservationDto reservationDto = new ReservationDto("n", LocalDate.of(3333, 3, 3), 1L, 1L);
        reservationService.saveReservation(reservationDto);
        assertThat(reservationService.findAllReservations()).hasSize(INITIAL_RESERVATION_COUNT + 1);
    }

    @DisplayName("예약을 삭제한다.")
    @Test
    void should_delete_reservation() {
        reservationService.deleteReservation(1);
        assertThat(reservationService.findAllReservations()).hasSize(INITIAL_RESERVATION_COUNT - 1);
    }

    @DisplayName("존재하는 예약을 삭제하려 하면 예외가 발생하지 않는다.")
    @Test
    void should_not_throw_exception_when_exist_reservation_time() {
        assertThatCode(() -> reservationService.deleteReservation(1))
                .doesNotThrowAnyException();
    }

    @DisplayName("존재하지 않는 예약을 삭제하려 하면 예외가 발생한다.")
    @Test
    void should_throw_exception_when_not_exist_reservation_time() {
        assertThatThrownBy(() -> reservationService.deleteReservation(999))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("[ERROR] 존재하지 않는 예약입니다.");
    }

    @DisplayName("현재 이전으로 예약하려 하면 예외가 발생한다.")
    @Test
    void should_throw_exception_when_previous_date() {
        ReservationDto reservationDto = new ReservationDto("n", LocalDate.of(999, 9, 9), 1L, 1L);
        assertThatThrownBy(() -> reservationService.saveReservation(reservationDto))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("[ERROR] 현재 이전 예약은 할 수 없습니다.");
    }

    @DisplayName("현재(날짜+시간)로 예약하려 하면 예외가 발생하지 않는다.")
    @Test
    void should_not_throw_exception_when_current_date() {
        ReservationDto reservationDto = new ReservationDto("n", LocalDate.now(), 3L, 1L);
        assertThatCode(() -> reservationService.saveReservation(reservationDto))
                .doesNotThrowAnyException();
    }

    @DisplayName("현재 이후로 예약하려 하면 예외가 발생하지 않는다.")
    @Test
    void should_not_throw_exception_when_later_date() {
        ReservationDto reservationDto = new ReservationDto("n", LocalDate.of(3333, 12, 31), 1L, 1L);
        assertThatCode(() -> reservationService.saveReservation(reservationDto))
                .doesNotThrowAnyException();
    }

    @DisplayName("날짜와 시간이 중복되는 예약을 추가하려 할 때 예외가 발생한다.")
    @Test
    void should_throw_exception_when_add_exist_reservation() {
        ReservationDto reservationDto = new ReservationDto("n", LocalDate.of(9999, 9, 9), 1L, 2L);
        assertThatThrownBy(() -> reservationService.saveReservation(reservationDto))
                .isInstanceOf(DuplicatedException.class)
                .hasMessage("[ERROR] 중복되는 예약은 추가할 수 없습니다.");
    }

    @DisplayName("예약 가능 상태를 담은 시간 정보를 반환한다.")
    @Test
    void should_return_times_with_book_state() {
        LocalDate date = LocalDate.of(9999, 9, 9);
        List<MemberReservationTimeResponse> times = reservationService.findReservationTimesInformation(date, 1);
        assertThat(times).hasSize(INITIAL_TIME_COUNT);
        assertThat(times).containsOnly(
                new MemberReservationTimeResponse(1, LocalTime.of(1, 0), true),
                new MemberReservationTimeResponse(2, LocalTime.of(2, 0), false),
                new MemberReservationTimeResponse(3, LocalTime.now().truncatedTo(ChronoUnit.SECONDS), false)
        );
    }
}
