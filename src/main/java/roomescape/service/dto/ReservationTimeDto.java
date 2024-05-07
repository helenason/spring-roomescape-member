package roomescape.service.dto;

import roomescape.controller.request.ReservationTimeRequest;

import java.time.LocalTime;

public class ReservationTimeDto {

    private Long id;
    private LocalTime startAt;

    public ReservationTimeDto(Long id, LocalTime startAt) {
        this.id = id;
        this.startAt = startAt;
    }

    public ReservationTimeDto(LocalTime startAt) {
        this.id = null;
        this.startAt = startAt;
    }

    public static ReservationTimeDto from(ReservationTimeRequest request) {
        return new ReservationTimeDto(null, request.getStartAt());
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }
}
