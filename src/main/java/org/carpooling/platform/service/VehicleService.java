package org.carpooling.platform.service;

import org.carpooling.platform.dto.request.VehicleRequestDTO;
import org.carpooling.platform.dto.response.VehicleResponseDTO;

import java.util.List;

public interface VehicleService {
    VehicleResponseDTO create(VehicleRequestDTO dto);
    VehicleResponseDTO findById(Long id);
    List<VehicleResponseDTO> findByDriverId(Long driverId);
    VehicleResponseDTO update(Long vehicleId, VehicleRequestDTO request);
    void delete(Long vehicleId);
}
