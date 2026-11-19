package org.carpooling.platform.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.carpooling.platform.enums.TripStatus;
import java.time.LocalDateTime;
import java.util.List;

public record TripRequestDTO(

        @NotNull(message = "Driver ID is required")
        Long driverId,

        @NotNull(message = "Vehicle ID is required")
        Long vehicleId,

        @Min(value = 1, message = "At least one seat must be offered")
        int seatsOffered,

        @NotNull(message = "Stopping points are required")
        @Size(min = 2, message = "A trip must have at least two stopping points")
        List<@Valid StoppingPointRequestDTO> stoppingPoints

) {}

