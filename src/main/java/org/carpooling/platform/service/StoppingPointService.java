package org.carpooling.platform.service;

import org.carpooling.platform.dto.request.StoppingPointRequestDTO;
import org.carpooling.platform.dto.response.StoppingPointResponseDTO;

import java.util.List;

public interface StoppingPointService {
    StoppingPointResponseDTO findById(Long id);

    List<StoppingPointResponseDTO> findByTripId(Long tripId);

    void validatePoints(List<StoppingPointRequestDTO> points);
}
