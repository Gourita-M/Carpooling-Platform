package org.carpooling.platform.dto.request;

public record BookingRequestDTO(
        Long passengerId,
        Long tripId,
        Long pickUpPointId,
        Long dropOffPointId,
        int numberOfSeats
) {}

