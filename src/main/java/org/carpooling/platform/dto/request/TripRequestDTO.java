package org.carpooling.platform.dto.request;

import org.carpooling.platform.enums.TripStatus;
import java.time.LocalDateTime;

public record TripRequestDTO(
        String departureCity,
        String destinationCity,
        LocalDateTime departureDate,
        int seatsOffered,
        TripStatus status
) {}

