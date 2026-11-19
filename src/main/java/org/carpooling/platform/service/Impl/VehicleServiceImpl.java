package org.carpooling.platform.service.Impl;

import lombok.RequiredArgsConstructor;
import org.carpooling.platform.dto.request.VehicleRequestDTO;
import org.carpooling.platform.dto.response.VehicleResponseDTO;
import org.carpooling.platform.entity.Driver;
import org.carpooling.platform.entity.Vehicle;
import org.carpooling.platform.exception.BusinessException;
import org.carpooling.platform.exception.ResourceNotFoundException;
import org.carpooling.platform.mapper.VehicleMapper;
import org.carpooling.platform.repository.DriverRepository;
import org.carpooling.platform.repository.VehicleRepository;
import org.carpooling.platform.service.VehicleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class VehicleServiceImpl implements VehicleService {
    private final VehicleRepository repository;
    private final DriverRepository driverRepository;
    private final VehicleMapper mapper;

    //done PostMan
    @Override
    public VehicleResponseDTO create(VehicleRequestDTO dto, Long driverId){
        Driver driver = driverRepository.findById(driverId).orElseThrow(() -> new ResourceNotFoundException("Driver is Not Found"));
        if(repository.existsByRegistrationNumber(dto.registrationNumber())){
            throw new BusinessException("Registration Number Already Exists");
        }
        Vehicle vehicle = mapper.toEntity(dto);
        vehicle.setDriver(driver);
        return mapper.toResponseDTO(repository.save(vehicle));
    }

    //done on PostMan
    @Override
    @Transactional(readOnly = true)
    public VehicleResponseDTO findById(Long id){
        Vehicle vehicle = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Vehicle is Not Found"));
        return mapper.toResponseDTO(vehicle);
    }

    //done on PostMan
    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponseDTO> findByDriverId(Long driverId){
        List<Vehicle> vehicles = repository.findAllByDriverId(driverId);
        return vehicles.stream().map(mapper::toResponseDTO).toList();
    }

    //done on PostMan
    @Override
    public VehicleResponseDTO update(Long vehicleId, VehicleRequestDTO request){
        Vehicle vehicle = repository.findById(vehicleId).orElseThrow(() -> new ResourceNotFoundException("Vehicle Is Not Found"));
        vehicle.setMake(request.make());
        vehicle.setModel(request.model());
        vehicle.setRegistrationNumber(request.registrationNumber());
        vehicle.setColor(request.color());
        vehicle.setCapacity(request.capacity());
        return mapper.toResponseDTO(repository.save(vehicle));
    }

    @Override
    public void delete(Long vehicleId){
        Vehicle vehicle = repository.findById(vehicleId).orElseThrow(() -> new ResourceNotFoundException("Vehicle Is Not Found"));
        repository.delete(vehicle);
    }
}
