package org.carpooling.platform.service;

import org.carpooling.platform.dto.request.PassengerRequestDTO;
import org.carpooling.platform.dto.response.PassengerResponseDTO;
import org.carpooling.platform.entity.Passenger;
import org.carpooling.platform.mapper.PassengerMapper;
import org.carpooling.platform.repository.PassengerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class PassengerServiceImpl implements PassengerService{
    private final PassengerRepository repository;
    private final PassengerMapper mapper;

    public PassengerServiceImpl(PassengerRepository repository, PassengerMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    //done on PostMan
    @Override
    public PassengerResponseDTO create(PassengerRequestDTO dto){
        Passenger passenger = mapper.toEntity(dto);
        return mapper.toResponseDTO(repository.save(passenger));
    }

    //done on PostMan
    @Override
    public PassengerResponseDTO findById(Long id){
        Passenger passenger = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Passenger is not Found"));
        return mapper.toResponseDTO(passenger);
    }

    @Override
    public Page<PassengerResponseDTO> findAll(Pageable pageable){
        Page<Passenger> passengers = repository.findAll(pageable);
        return passengers.map(mapper::toResponseDTO);
    }

    @Override
    public Page<PassengerResponseDTO> search(String keyword, Pageable pageable){
        return null;
    }

    @Override
    public PassengerResponseDTO update(Long id, PassengerRequestDTO request){
        return null;
    }

    @Override
    public void delete(Long id){

    }
}
