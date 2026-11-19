package org.carpooling.platform.service;

import org.carpooling.platform.dto.request.BookingRequestDTO;
import org.carpooling.platform.dto.response.BookingResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface BookingService {
    BookingResponseDTO create(BookingRequestDTO dto);
    BookingResponseDTO findById(Long id);
    Page<BookingResponseDTO> findByPassengerId(
            Long passengerId,
            Pageable pageable
    );
    Page<BookingResponseDTO> findByTripId(
            Long tripId,
            Pageable pageable
    );
    BookingResponseDTO cancel(Long bookingId, Long passengerId);
    BigDecimal calculatePrice(
            Long pickUpPointId,
            Long dropOffPointId,
            int numberOfSeats
    );
    int getRemainingSeats(Long tripId);
}
