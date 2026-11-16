package org.carpooling.platform.service;

import org.carpooling.platform.dto.request.StoppingPointRequestDTO;
import org.carpooling.platform.dto.response.StoppingPointResponseDTO;

public interface StoppingPointService {
    StoppingPointResponseDTO create(StoppingPointRequestDTO dto);
}
