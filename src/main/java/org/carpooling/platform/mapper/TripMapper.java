package org.carpooling.platform.mapper;

import org.carpooling.platform.dto.request.TripRequestDTO;
import org.carpooling.platform.dto.response.TripResponseDTO;
import org.carpooling.platform.entity.Trip;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        componentModel = "spring",
        uses = StoppingPointMapper.class
)
public interface TripMapper {

    @Mapping(target = "id", ignore = true)
    Trip toEntity(TripRequestDTO request);

    @Mapping(source = "driver.id", target = "driverId")
    @Mapping(source = "vehicle.id", target = "vehicleId")
    @Mapping(source = "remainingSeats", target = "remainingSeats")
    TripResponseDTO toResponseDTO(Trip trip);
}

