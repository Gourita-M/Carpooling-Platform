package org.carpooling.platform.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class DriverServiceImpl implements DriverService {
    private final DriverRepository repository;
    private final DriverMapper mapper;

    //done on Postman
    @Override
    public DriverResponseDTO create(DriverRequestDTO dto){
    log.info("Creating New Driver Email: {}", dto.email());
            Driver driver = mapper.toEntity(dto);
            return mapper.toResponseDTO(repository.save(driver));
    }
    //done on Postman
    @Override
    @Transactional(readOnly = true)
    public DriverResponseDTO findById(Long id){
    log.info("Getting Driver With ID: {}", id);
        Driver driver = repository.findById(id).orElseThrow( () -> {
            log.warn("Driver ID: {}, is Not Found", id);
            return new IllegalArgumentException("Driver is not Found");
        });
        return mapper.toResponseDTO(driver);
    }

    //done on Postman
    @Override
    @Transactional(readOnly = true)
    public Page<DriverResponseDTO> findAll(Pageable pageable){
    log.info("Getting All Drivers");
        Page<Driver> drivers = repository.findAll(pageable);
        return drivers.map(mapper::toResponseDTO);
    }

    //done on Postman
    @Override
    @Transactional(readOnly = true)
    public Page<DriverResponseDTO> search(String keyword, Pageable pageable){
    log.info("Searching For Driver With KeyWord: {}", keyword);
        Page<Driver> drivers =
                repository.findByFirstNameContainingIgnoreCaseOrEmailContainingIgnoreCase(keyword, keyword, pageable
                );
        return drivers.map(mapper::toResponseDTO);
    }

    //done on Postman
    @Override
    public DriverResponseDTO update(Long id, DriverRequestDTO request){
    log.info("Updating Info of Driver ID: {}", id);
        Driver driver = repository.findById(id).orElseThrow(() -> {
            log.warn("Driver ID: {}, Can't Be Found To Be Updated", id);
            return new IllegalArgumentException("There is No Driver With this ID");
        });
        Driver updated = mapper.toEntity(request);
        updated.setId(id);
        return mapper.toResponseDTO(repository.save(updated));
    }

    //done on Postman
    @Override
    public void delete(Long id){
    log.info("Deleting Driver ID: {}",id);
        Driver driver = repository.findById(id).orElseThrow(() -> {
            log.warn("Driver ID: {}, Can't Be Found To Be Deleted", id);
            return new IllegalArgumentException("There is No Driver With this ID");
        });
        repository.delete(driver);
    }

}
