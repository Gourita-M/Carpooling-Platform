package org.carpooling.platform.mapper;

import org.carpooling.platform.dto.request.StoppingPointRequestDTO;
import org.carpooling.platform.dto.response.StoppingPointResponseDTO;
import org.carpooling.platform.entity.StoppingPoint;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StoppingPointMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trip", ignore = true)
    StoppingPoint toEntity(StoppingPointRequestDTO request);

    StoppingPointResponseDTO toResponseDTO(
            StoppingPoint stoppingPoint
    );
}

