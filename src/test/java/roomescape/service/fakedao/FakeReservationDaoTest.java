package roomescape.service.fakedao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.repository.dto.ReservationSavedDto;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

class FakeReservationDaoTest {

    private static final int INITIAL_RESERVATION_COUNT = 3;
    private static final List<ReservationSavedDto> INITIAL_RESERVATIONS = List.of(
            new ReservationSavedDto(1, "n1", LocalDate.now(), 1, 1),
            new ReservationSavedDto(2, "n2", LocalDate.now(), 1, 2),
            new ReservationSavedDto(3, "n3", LocalDate.now(), 1, 3)
    );

    private FakeReservationDao fakeReservationDao;

    @BeforeEach
    void setUp() {
        fakeReservationDao = new FakeReservationDao(INITIAL_RESERVATIONS);
    }

    @DisplayName("전체 예약을 조회한다.")
    @Test
    void should_find_all_reservations() {
        List<ReservationSavedDto> allReservations = fakeReservationDao.findAll();
        assertThat(allReservations).hasSize(INITIAL_RESERVATION_COUNT);
        assertThat(allReservations).isEqualTo(INITIAL_RESERVATIONS);
    }

    @DisplayName("예약을 저장한 후 id를 반환한다.")
    @Test
    void should_save_reservation_time() {
        ReservationSavedDto reservationSavedDto = new ReservationSavedDto(4, "n4", LocalDate.now(), 1, 1);
        long id = fakeReservationDao.save(reservationSavedDto);
        assertThat(id).isEqualTo(INITIAL_RESERVATION_COUNT + 1);
        assertThat(fakeReservationDao.findAll()).hasSize(INITIAL_RESERVATION_COUNT + 1);
    }

    @DisplayName("특정 id를 가진 예약을 조회한다.")
    @Test
    void should_find_reservation_by_id() {
        Optional<ReservationSavedDto> time = fakeReservationDao.findById(1);
        assertThat(time).hasValue(new ReservationSavedDto(1, "n1", LocalDate.now(), 1, 1));
    }

    @DisplayName("유효하지 않은 id를 가진 예약을 조회하려는 경우 빈 Optional 을 반환한다.")
    @Test
    void should_return_empty_optional_when_not_exist_id() {
        Optional<ReservationSavedDto> reservationSavedDto = fakeReservationDao.findById(999);
        assertThat(reservationSavedDto).isEmpty();
    }

    @DisplayName("특정 날짜와 테마 id를 가진 예약을 조회한다.")
    @Test
    void should_find_reservations_by_date_and_themeId() {
        List<ReservationSavedDto> reservations = fakeReservationDao.findByDateAndThemeId(LocalDate.now(), 1);
        assertThat(reservations).containsOnly(new ReservationSavedDto(1, "n1", LocalDate.now(), 1, 1));
    }

    @DisplayName("해당 날짜와 테마 id를 가진 예약이 없는 경우 빈 리스트를 반환한다.")
    @Test
    void should_return_empty_list_when_not_exist() {
        List<ReservationSavedDto> reservations = fakeReservationDao.findByDateAndThemeId(LocalDate.now(), 999);
        assertThat(reservations).isEmpty();
    }

    @DisplayName("특정 날짜에 해당하는 예약의 테마 id를, 개수 순 -> 테마 id 순으로 정렬하여, 수를 추려 반환한다.")
    @Test
    void findThemeIdByDateAndOrderByThemeIdCountAndLimit() {
        List<Long> themeIds = fakeReservationDao.findThemeIdByDateAndOrderByThemeIdCountAndLimit(LocalDate.of(2000, 1, 1), LocalDate.now(), 2);
        assertThat(themeIds).containsExactly(1L, 2L);
    }

    @DisplayName("예약을 삭제한다.")
    @Test
    void should_delete_reservation() {
        fakeReservationDao.deleteById(1);
        assertThat(fakeReservationDao.findById(1)).isEmpty();
        assertThat(fakeReservationDao.findAll()).hasSize(INITIAL_RESERVATION_COUNT - 1);
    }

    @DisplayName("유효한 id를 가진 예약을 삭제하려는 경우 예외가 발생하지 않는다.")
    @Test
    void should_not_throw_exception_when_exist_id() {
        assertThatCode(() -> fakeReservationDao.deleteById(1))
                .doesNotThrowAnyException();
    }

    @DisplayName("유효하지 않은 id를 가진 예약을 삭제하려는 경우 예외가 발생한다.")
    @Test
    void should_throw_exception_when_not_exist_id() {
        assertThatThrownBy(() -> fakeReservationDao.deleteById(999))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("존재하지 않는 예약입니다.");
    }

    @DisplayName("해당 id를 가진 예약 시간이 존재하면 참을 반환한다.")
    @Test
    void should_return_true_when_exist_id() {
        Boolean isExist = fakeReservationDao.isExistById(1);
        assertThat(isExist).isTrue();
    }

    @DisplayName("해당 id를 가진 예약 시간이 존재하지 않으면 거짓을 반환한다.")
    @Test
    void should_return_false_when_not_exist_id() {
        Boolean isExist = fakeReservationDao.isExistById(999);
        assertThat(isExist).isFalse();
    }

    @DisplayName("해당 timeId를 가진 예약이 존재하면 참을 반환한다.")
    @Test
    void should_return_true_when_exist_timeId() {
        Boolean isExist = fakeReservationDao.isExistByTimeId(1);
        assertThat(isExist).isTrue();
    }

    @DisplayName("해당 timeId를 가진 예약이 존재하지 않으면 거짓을 반환한다.")
    @Test
    void should_return_false_when_not_exist_timeId() {
        Boolean isExist = fakeReservationDao.isExistByTimeId(999);
        assertThat(isExist).isFalse();
    }

    @DisplayName("해당 날짜와 timeId를 가진 예약이 존재하면 참을 반환한다.")
    @Test
    void should_return_true_when_exist_date_and_timeId() {
        Boolean isExist = fakeReservationDao.isExistByDateAndTimeIdAndThemeId(LocalDate.now(), 1L, 1L);
        assertThat(isExist).isTrue();
    }

    @DisplayName("해당 날짜와 timeId를 가진 예약이 존재하지 않으면 거짓을 반환한다.")
    @Test
    void should_return_false_when_not_exist_date_and_timeId() {
        Boolean isExist = fakeReservationDao.isExistByDateAndTimeIdAndThemeId(LocalDate.now(), 999L, 1L);
        assertThat(isExist).isFalse();
    }
}