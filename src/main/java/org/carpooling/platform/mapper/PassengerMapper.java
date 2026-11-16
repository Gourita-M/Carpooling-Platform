package org.carpooling.platform.mapper;

import org.carpooling.platform.dto.request.PassengerRequestDTO;
import org.carpooling.platform.dto.response.PassengerResponseDTO;
import org.carpooling.platform.entity.Passenger;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PassengerMapper {

    Passenger toEntity(PassengerRequestDTO request);

    PassengerResponseDTO toResponseDTO(Passenger passenger);
}

