package org.carpooling.platform.mapper;

import org.carpooling.platform.dto.request.VehicleRequestDTO;
import org.carpooling.platform.dto.response.VehicleResponseDTO;
import org.carpooling.platform.entity.Vehicle;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VehicleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "driver", ignore = true)
    @Mapping(target = "trips", ignore = true)
    Vehicle toEntity(VehicleRequestDTO request);

    @Mapping(source = "driver.id", target = "driverId")
    VehicleResponseDTO toResponseDTO(Vehicle vehicle);
}

