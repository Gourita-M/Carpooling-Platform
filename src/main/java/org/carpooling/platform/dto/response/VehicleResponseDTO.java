package org.carpooling.platform.dto.response;

public record VehicleResponseDTO(
        Long id,
        String make,
        String model,
        String registrationNumber,
        String color,
        int capacity,
        Long driverId
) {}

