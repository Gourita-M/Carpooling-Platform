package org.carpooling.platform.service;

import org.carpooling.platform.dto.request.DriverRequestDTO;
import org.carpooling.platform.dto.response.DriverResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DriverService {
    DriverResponseDTO create(DriverRequestDTO dto);
    DriverResponseDTO findById(Long id);
    Page<DriverResponseDTO> findAll(Pageable pageable);
    Page<DriverResponseDTO> search(String keyword, Pageable pageable);
    DriverResponseDTO update(Long id, DriverRequestDTO request);
    void delete(Long id);
}
