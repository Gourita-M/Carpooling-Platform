package org.carpooling.platform.dto.response;

import org.carpooling.platform.enums.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BookingResponseDTO(
        Long id,
        Long passengerId,
        Long tripId,
        Long pickUpPointId,
        Long dropOffPointId,
        int numberOfSeats,
        BigDecimal totalAmount,
        LocalDateTime bookingDate,
        BookingStatus status
) {}

