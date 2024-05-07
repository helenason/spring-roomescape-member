package roomescape.controller.request;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.exception.BadRequestException;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationRequestTest {

    @DisplayName("요청된 데이터의 예약자명이 null 혹은 비어있는 경우 예외를 발생시킨다.")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\n", "\t"})
    void should_throw_exception_when_invalid_name(String name) {
        assertThatThrownBy(() -> new ReservationRequest(name, LocalDate.now(), 1L, 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("[ERROR] 요청된 데이터에 null 혹은 비어있는 값이 존재합니다.");
    }

    @DisplayName("요청된 데이터의 날짜가 null인 경우 예외를 발생시킨다.")
    @Test
    void should_throw_exception_when_invalid_date() {
        assertThatThrownBy(() -> new ReservationRequest("n", null, 1L, 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("[ERROR] 요청된 데이터에 null 혹은 비어있는 값이 존재합니다.");
    }

    @DisplayName("요청된 데이터의 시간 id가 null인 경우 예외를 발생시킨다.")
    @Test
    void should_throw_exception_when_invalid_timeId() {
        assertThatThrownBy(() -> new ReservationRequest("n", LocalDate.now(), null, 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("[ERROR] 요청된 데이터에 null 혹은 비어있는 값이 존재합니다.");
    }

    @DisplayName("요청된 데이터의 테마 id가 null인 경우 예외를 발생시킨다.")
    @Test
    void should_throw_exception_when_invalid_themeId() {
        assertThatThrownBy(() -> new ReservationRequest("n", LocalDate.now(), 1L, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("[ERROR] 요청된 데이터에 null 혹은 비어있는 값이 존재합니다.");
    }
}
