package roomescape.controller.request;

import roomescape.exception.BadRequestException;

import java.time.LocalTime;

public class ReservationTimeRequest {

    private LocalTime startAt;

    public ReservationTimeRequest(LocalTime startAt) {
        validateTime(startAt);
        this.startAt = startAt;
    }

    private ReservationTimeRequest() {
    }

    private void validateTime(LocalTime startAt) {
        if (startAt == null) {
            throw new BadRequestException("[ERROR] 유효하지 않은 요청입니다.");
        }
    }

    public LocalTime getStartAt() {
        return startAt;
    }
}
