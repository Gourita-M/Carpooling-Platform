package org.carpooling.platform.service;

import org.carpooling.platform.dto.request.TripRequestDTO;
import org.carpooling.platform.dto.response.TripResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface TripService {
    TripResponseDTO create(TripRequestDTO dto,Long driverId, Long vehicleId);
    TripResponseDTO findById(Long id);
    Page<TripResponseDTO> findAll(Pageable pageable);
    Page<TripResponseDTO> findByDriverId(Long driverId, Pageable pageable);
    Page<TripResponseDTO> search(
            String departureCity,
            String destinationCity,
            LocalDate departureDate,
            Long numberOfSeats,
            Pageable pageable
    );
    TripResponseDTO update(Long tripId, TripRequestDTO request, Long driverId);
    TripResponseDTO complete(Long tripId, Long driverId);
    void cancel(Long tripId, Long driverId);
//    int getRemainingSeats(Long tripId);
}
