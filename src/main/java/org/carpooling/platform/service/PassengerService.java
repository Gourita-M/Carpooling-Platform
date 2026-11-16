package org.carpooling.platform.service;

import org.carpooling.platform.dto.request.PassengerRequestDTO;
import org.carpooling.platform.dto.response.PassengerResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface PassengerService {
    PassengerResponseDTO create(PassengerRequestDTO dto);
    PassengerResponseDTO findById(Long id);
    Page<PassengerResponseDTO> findAll(Pageable pageable);
    Page<PassengerResponseDTO> search(String keyword, Pageable pageable);
    PassengerResponseDTO update(Long id, PassengerRequestDTO request);
    void delete(Long id);
}
