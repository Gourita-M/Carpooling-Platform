package org.carpooling.platform.service;

import org.carpooling.platform.dto.request.DriverRequestDTO;
import org.carpooling.platform.dto.response.DriverResponseDTO;
import org.carpooling.platform.repository.DriverRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public class DriverServiceImpl implements DriverService{
    private final DriverRepository repository;

    public DriverServiceImpl(DriverRepository repository) {
        this.repository = repository;
    }

    @Override
    public DriverResponseDTO create(DriverRequestDTO dto){
        return null;
    }

    @Override
    public DriverResponseDTO findById(Long id){
        return null;
    }

    @Override
    public Page<DriverResponseDTO> findAll(Pageable pageable){
        return null;
    }

    @Override
    public Page<DriverResponseDTO> search(String keyword, Pageable pageable){
        return null;
    }

    @Override
    public DriverResponseDTO update(Long id, DriverRequestDTO request){
        return null;
    }

    @Override
    public void delete(Long id){

    }

}
