package org.carpooling.platform.service;

import lombok.RequiredArgsConstructor;
import org.carpooling.platform.dto.request.TripRequestDTO;
import org.carpooling.platform.dto.response.TripResponseDTO;
import org.carpooling.platform.entity.Driver;
import org.carpooling.platform.entity.Trip;
import org.carpooling.platform.entity.Vehicle;
import org.carpooling.platform.enums.TripStatus;
import org.carpooling.platform.mapper.TripMapper;
import org.carpooling.platform.repository.BookingRepository;
import org.carpooling.platform.repository.DriverRepository;
import org.carpooling.platform.repository.TripRepository;
import org.carpooling.platform.repository.VehicleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class TripServiceImpl implements TripService{
    private final TripRepository repository;
    private final TripMapper mapper;
    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final BookingRepository bookingRepository;

    //done
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

    //done
    @Override
    @Transactional(readOnly = true)
    public TripResponseDTO findById(Long id){
        Trip trip = repository.findById(id).orElseThrow(()-> new IllegalArgumentException("Trip Can't Be Found"));
        return mapper.toResponseDTO(trip);
    }

    //Done
    @Override
    @Transactional(readOnly = true)
    public Page<TripResponseDTO> findAll(Pageable pageable){
        Page<Trip> trips = repository.findAll(pageable);
        return trips.map(mapper::toResponseDTO);
    }

    //Done PostMan
    @Override
    @Transactional(readOnly = true)
    public Page<TripResponseDTO> findByDriverId(Long driverId, Pageable pageable){
        Page<Trip> trips = repository.findByDriverId(driverId, pageable);
        return trips.map(mapper::toResponseDTO);
    }

    //needs to be Tested
    @Override
    @Transactional(readOnly = true)
    public Page<TripResponseDTO> search(
            String departureCity,
            String destinationCity,
            LocalDate departureDate,
            Long numberOfSeats,
            Pageable pageable
    ){
        Page<Trip> trips =
                repository.findByDepartureCityContainingIgnoreCaseOrDestinationCityContainingIgnoreCaseOrDepartureDateContainingIgnoreCaseOrSeatsOfferedContainingIgnoreCase(
                        departureCity,
                        destinationCity,
                        departureDate,
                        numberOfSeats,
                        pageable
                );
        return trips.map(mapper::toResponseDTO);
    }

    //done PostMan
    @Override
    public TripResponseDTO update(Long tripId, TripRequestDTO request, Long driverId){
        Trip trip = repository.findByIdAndDriver_Id(tripId, driverId);
        trip.setDepartureCity(request.departureCity());
        trip.setDestinationCity(request.destinationCity());
        trip.setDepartureDate(request.departureDate());
        trip.setSeatsOffered(request.seatsOffered());
        trip.setStatus(request.status());
        return mapper.toResponseDTO(repository.save(trip));
    }

    //done PostMan
    @Override
    public TripResponseDTO complete(Long tripId, Long driverId){
        Trip trip = repository.findByIdAndDriver_Id(tripId, driverId);
        if(trip == null){
            throw new IllegalArgumentException("Trip Can't Be Found");
        }
        trip.setStatus(TripStatus.COMPLETED);
        return mapper.toResponseDTO(repository.save(trip));
    }

    //done PostMan
    @Override
    public void cancel(Long tripId, Long driverId){
        Trip trip = repository.findByIdAndDriver_Id(tripId, driverId);
        if(trip == null){
            throw new IllegalArgumentException("Trip Can't Be Found");
        }
        trip.setStatus(TripStatus.CANCELLED);
        repository.save(trip);
    }


//    @Override
//    @Transactional(readOnly = true)
//    public int getRemainingSeats(Long tripId){
//        Trip trip = repository.findById(tripId)
//                .orElseThrow(() -> new IllegalArgumentException("Trip not found"));
//        int bookedSeats = bookingRepository.sumConfirmedSeatsByTripId(tripId);
//        return trip.getSeatsOffered() - bookedSeats;
//    }
}
