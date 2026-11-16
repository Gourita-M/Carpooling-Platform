package org.carpooling.platform.service;

import org.carpooling.platform.dto.request.PassengerRequestDTO;
import org.carpooling.platform.dto.response.PassengerResponseDTO;
import org.carpooling.platform.repository.PassengerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class PassengerServiceImpl implements PassengerService{
    private final PassengerRepository repository;

    public PassengerServiceImpl(PassengerRepository repository) {
        this.repository = repository;
    }

    @Override
    public PassengerResponseDTO create(PassengerRequestDTO dto){
        return null;
    }

    @Override
    public PassengerResponseDTO findById(Long id){
        return null;
    }

    @Override
    public Page<PassengerResponseDTO> findAll(Pageable pageable){
        return null;
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
