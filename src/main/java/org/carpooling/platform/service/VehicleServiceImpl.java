package org.carpooling.platform.service;

import org.carpooling.platform.dto.request.VehicleRequestDTO;
import org.carpooling.platform.dto.response.VehicleResponseDTO;
import org.carpooling.platform.entity.Driver;
import org.carpooling.platform.entity.Vehicle;
import org.carpooling.platform.mapper.DriverMapper;
import org.carpooling.platform.mapper.VehicleMapper;
import org.carpooling.platform.repository.DriverRepository;
import org.carpooling.platform.repository.VehicleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VehicleServiceImpl implements VehicleService{
    private final VehicleRepository repository;
    private final DriverRepository driverRepository;
    private final VehicleMapper mapper;

    public VehicleServiceImpl(VehicleRepository repository, DriverRepository driverRepository, VehicleMapper mapper) {
        this.repository = repository;
        this.driverRepository = driverRepository;
        this.mapper = mapper;
    }

    //done PostMan
    @Override
    public VehicleResponseDTO create(VehicleRequestDTO dto, Long driverId){
        Driver driver = driverRepository.findById(driverId).orElseThrow(() -> new IllegalArgumentException("Driver is Not Found"));
        if(repository.existsByRegistrationNumber(dto.registrationNumber())){
            throw new IllegalArgumentException("Registration Number Already Exists");
        }
        Vehicle vehicle = mapper.toEntity(dto);
        vehicle.setDriver(driver);
        return mapper.toResponseDTO(repository.save(vehicle));
    }

    //done on PostMan
    @Override
    public VehicleResponseDTO findById(Long id){
        Vehicle vehicle = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Vehicle is Not Found"));
        return mapper.toResponseDTO(vehicle);
    }

    //done on PostMan
    @Override
    public List<VehicleResponseDTO> findByDriverId(Long driverId){
        List<Vehicle> vehicles = repository.findAllByDriverId(driverId);
        return vehicles.stream().map(mapper::toResponseDTO).toList();
    }

    //done on PostMan
    @Override
    public VehicleResponseDTO update(Long vehicleId, VehicleRequestDTO request){
        Vehicle vehicle = repository.findById(vehicleId).orElseThrow(() -> new IllegalArgumentException("Vehicle Is Not Found"));
        Vehicle updated = mapper.toEntity(request);
        updated.setId(vehicleId);
        return mapper.toResponseDTO(repository.save(updated));
    }

    @Override
    public void delete(Long vehicleId){

    }
}
