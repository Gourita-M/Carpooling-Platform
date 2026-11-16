package org.carpooling.platform.service;

import org.carpooling.platform.dto.request.TripRequestDTO;
import org.carpooling.platform.dto.response.TripResponseDTO;
import org.carpooling.platform.repository.TripRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class TripServiceImpl implements TripService{
    private final TripRepository repository;

    public TripServiceImpl(TripRepository repository) {
        this.repository = repository;
    }

    @Override
    public TripResponseDTO create(TripRequestDTO dto, Long driverId){
        return null;
    }

    @Override
    public TripResponseDTO findById(Long id){
        return null;
    }

    @Override
    public Page<TripResponseDTO> findAll(Pageable pageable){
        return null;
    }

    @Override
    public Page<TripResponseDTO> findByDriverId(Long driverId, Pageable pageable){
        return null;
    }

    @Override
    public Page<TripResponseDTO> search(
            String departureCity,
            String destinationCity,
            LocalDate date,
            Integer numberOfSeats,
            BigDecimal maxPrice,
            Pageable pageable
    ){
        return null;
    }

    @Override
    public TripResponseDTO update(Long tripId, TripRequestDTO request, Long driverId){
        return null;
    }

    @Override
    public TripResponseDTO complete(Long tripId, Long driverId){
        return null;
    }

    @Override
    public void cancel(Long tripId, Long driverId){

    }

    @Override
    public int getRemainingSeats(Long tripId){
        return 0;
    }
}
