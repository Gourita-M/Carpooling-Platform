package org.carpooling.platform.service;

import org.carpooling.platform.dto.request.BookingRequestDTO;
import org.carpooling.platform.dto.response.BookingResponseDTO;
import org.carpooling.platform.repository.BookingRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
public class BookingServiceImpl implements BookingService{
    private final BookingRepository repository;

    public BookingServiceImpl(BookingRepository repository) {
        this.repository = repository;
    }

    @Override
    public BookingResponseDTO create(BookingRequestDTO dto){
        return null;
    }

    @Override
    public BookingResponseDTO findById(Long id){
        return null;
    }

    @Override
    public Page<BookingResponseDTO> findByPassengerId(
            Long passengerId,
            Pageable pageable
    ){
        return null;
    }

    @Override
    public Page<BookingResponseDTO> findByTripId(
            Long tripId,
            Pageable pageable
    ){
        return null;
    }

    @Override
    public BookingResponseDTO cancel(Long bookingId, Long passengerId){
        return null;
    }

    @Override
    public BigDecimal calculatePrice(
            Long pickUpPointId,
            Long dropOffPointId,
            int numberOfSeats
    ){
        return null;
    }

    @Override
    public int getRemainingSeats(Long tripId){
        return 0;
    }
}
