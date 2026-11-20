package org.carpooling.platform.service.impl;

import lombok.RequiredArgsConstructor;
import org.carpooling.platform.dto.request.DriverRequestDTO;
import org.carpooling.platform.dto.response.DriverResponseDTO;
import org.carpooling.platform.entity.Driver;
import org.carpooling.platform.mapper.DriverMapper;
import org.carpooling.platform.repository.DriverRepository;
import org.carpooling.platform.service.DriverService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {
    private final DriverRepository repository;
    private final DriverMapper mapper;

    //done on Postman
    @Override
    public DriverResponseDTO create(DriverRequestDTO dto){
            Driver driver = mapper.toEntity(dto);
            return mapper.toResponseDTO(repository.save(driver));
    }
    //done on Postman
    @Override
    @Transactional(readOnly = true)
    public DriverResponseDTO findById(Long id){
        Driver driver = repository.findById(id).orElseThrow( () -> new IllegalArgumentException("Driver is not Found"));
        return mapper.toResponseDTO(driver);
    }

    //done on Postman
    @Override
    @Transactional(readOnly = true)
    public Page<DriverResponseDTO> findAll(Pageable pageable){
        Page<Driver> drivers = repository.findAll(pageable);
        return drivers.map(mapper::toResponseDTO);
    }

    //done on Postman
    @Override
    @Transactional(readOnly = true)
    public Page<DriverResponseDTO> search(String keyword, Pageable pageable){
        Page<Driver> drivers =
                repository.findByFirstNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                        keyword,
                        keyword,
                        pageable
                );

        return drivers.map(mapper::toResponseDTO);
    }

    //done on Postman
    @Override
    public DriverResponseDTO update(Long id, DriverRequestDTO request){
        Driver driver = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("There is No Driver With this ID"));
        Driver updated = mapper.toEntity(request);
        updated.setId(id);
        return mapper.toResponseDTO(repository.save(updated));
    }

    //done on Postman
    @Override
    public void delete(Long id){
        Driver driver = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("There is No Driver With this ID"));
        repository.delete(driver);
    }

}
