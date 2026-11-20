package org.carpooling.platform.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.carpooling.platform.dto.request.PassengerRequestDTO;
import org.carpooling.platform.dto.response.PassengerResponseDTO;
import org.carpooling.platform.entity.Passenger;
import org.carpooling.platform.mapper.PassengerMapper;
import org.carpooling.platform.repository.PassengerRepository;
import org.carpooling.platform.service.PassengerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PassengerServiceImpl implements PassengerService {
    private final PassengerRepository repository;
    private final PassengerMapper mapper;

    //done on PostMan
    @Override
    public PassengerResponseDTO create(PassengerRequestDTO dto){
    log.info("Creating New Passenger Email: {}", dto.email());
        Passenger passenger = mapper.toEntity(dto);
        return mapper.toResponseDTO(repository.save(passenger));
    }

    //done on PostMan
    @Override
    @Transactional(readOnly = true)
    public PassengerResponseDTO findById(Long id){
    log.info("Finding A Passenger With ID: {}",id);
        Passenger passenger = repository.findById(id).orElseThrow(() -> {
            log.warn("Passenger ID: {}, Can't Be Found", id);
            return new IllegalArgumentException("Passenger is not Found");
        });
        return mapper.toResponseDTO(passenger);
    }

    //done on PostMan
    @Override
    @Transactional(readOnly = true)
    public Page<PassengerResponseDTO> findAll(Pageable pageable){
    log.info("Getting All Passengers");
        Page<Passenger> passengers = repository.findAll(pageable);
        return passengers.map(mapper::toResponseDTO);
    }

    //done
    @Override
    @Transactional(readOnly = true)
    public Page<PassengerResponseDTO> search(String email, Pageable pageable){
    log.info("Searching for Passenger With Email: {}", email);
        Page<Passenger> passengers = repository.findByEmailContainingIgnoreCase(email, pageable);
        return passengers.map(mapper::toResponseDTO);
    }

    //done on PostMan
    @Override
    public PassengerResponseDTO update(Long id, PassengerRequestDTO request){
    log.info("Updating Info of Passenger: {}", id);
        Passenger passenger = repository.findById(id).orElseThrow(() -> {
            log.warn("Passenger: {}, Can't be Found To Be Updated", id);
            return new IllegalArgumentException("Passenger Is Not Found");
        });
        Passenger updated = mapper.toEntity(request);
        updated.setId(id);
        return mapper.toResponseDTO(repository.save(updated));
    }

    //done on PostMan
    @Override
    public void delete(Long id){
    log.info("Deleting Passenger: {}", id);
        Passenger passenger = repository.findById(id).orElseThrow(() -> {
            log.warn("Passenger: {}, Can't Be Found To Get Deleted", id);
            return new IllegalArgumentException("Passenger Is Not Found");
        });
        repository.delete(passenger);
    }
}
