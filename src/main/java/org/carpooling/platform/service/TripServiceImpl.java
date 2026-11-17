package org.carpooling.platform.service;

import org.carpooling.platform.dto.request.TripRequestDTO;
import org.carpooling.platform.dto.response.TripResponseDTO;
import org.carpooling.platform.entity.Driver;
import org.carpooling.platform.entity.Trip;
import org.carpooling.platform.entity.Vehicle;
import org.carpooling.platform.mapper.TripMapper;
import org.carpooling.platform.repository.DriverRepository;
import org.carpooling.platform.repository.TripRepository;
import org.carpooling.platform.repository.VehicleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class TripServiceImpl implements TripService{
    private final TripRepository repository;
    private final TripMapper mapper;
    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;

    public TripServiceImpl(TripRepository repository, TripMapper mapper, VehicleRepository vehicleRepository, DriverRepository driverRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.vehicleRepository = vehicleRepository;
        this.driverRepository = driverRepository;
    }

    @Override
    public TripResponseDTO create(TripRequestDTO dto, Long driverId,Long vehicleId){
        Driver driver = driverRepository.findById(driverId).orElseThrow(()-> new IllegalArgumentException("Driver is Not Found"));
        Vehicle vehicle = vehicleRepository.findAllByDriverId(driverId)
                .stream().filter(v -> v.getId().equals(vehicleId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Vehicle not found for this driver"));
        Trip trip = mapper.toEntity(dto);
        trip.setDriver(driver);
        trip.setVehicle(vehicle);
        return mapper.toResponseDTO(repository.save(trip));
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
