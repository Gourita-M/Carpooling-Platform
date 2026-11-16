package org.carpooling.platform.mapper;

import org.carpooling.platform.dto.request.DriverRequestDTO;
import org.carpooling.platform.dto.response.DriverResponseDTO;
import org.carpooling.platform.entity.Driver;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DriverMapper {

    Driver toEntity(DriverRequestDTO request);

    DriverResponseDTO toResponseDTO(Driver driver);
}

