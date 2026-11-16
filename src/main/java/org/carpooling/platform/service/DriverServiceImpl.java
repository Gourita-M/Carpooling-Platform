package org.carpooling.platform.service;

import org.carpooling.platform.dto.request.DriverRequestDTO;
import org.carpooling.platform.dto.response.DriverResponseDTO;
import org.carpooling.platform.entity.Driver;
import org.carpooling.platform.mapper.DriverMapper;
import org.carpooling.platform.repository.DriverRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class DriverServiceImpl implements DriverService{
    private final DriverRepository repository;
    private final DriverMapper mapper;

    public DriverServiceImpl(DriverRepository repository, DriverMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }
    //done on Postman
    @Override
    public DriverResponseDTO create(DriverRequestDTO dto){
            Driver driver = mapper.toEntity(dto);
            return mapper.toResponseDTO(repository.save(driver));
    }
    //done on Postman
    @Override
    public DriverResponseDTO findById(Long id){
        Driver driver = repository.findById(id).orElseThrow( () -> new IllegalArgumentException("Driver is not Found"));
        return mapper.toResponseDTO(driver);
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
