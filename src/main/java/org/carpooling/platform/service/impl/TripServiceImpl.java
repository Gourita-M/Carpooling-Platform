
package org.carpooling.platform.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.carpooling.platform.dto.request.TripRequestDTO;
import org.carpooling.platform.dto.response.TripResponseDTO;
import org.carpooling.platform.entity.*;
import org.carpooling.platform.enums.BookingStatus;
import org.carpooling.platform.enums.TripStatus;
import org.carpooling.platform.exception.BusinessException;
import org.carpooling.platform.exception.ResourceNotFoundException;
import org.carpooling.platform.mapper.TripMapper;
import org.carpooling.platform.repository.*;
import org.carpooling.platform.service.TripService;
import org.carpooling.platform.service.StoppingPointService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;
    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final BookingRepository bookingRepository;
    private final TripMapper tripMapper;
    private final StoppingPointService stoppingPointService;

    //Done
    @Override
    public TripResponseDTO create(TripRequestDTO request) {
    log.info("Creating New Trip");
        Driver driver = driverRepository.findById(request.driverId()).orElseThrow(() -> {
            log.warn("Driver ID: {}, Can't Be Found", request.driverId());
            return new ResourceNotFoundException("Driver not found");
        });

        Vehicle vehicle = vehicleRepository.findById(request.vehicleId()).orElseThrow(() -> {
            log.warn("Vehicle ID: {}, Can't Be Found", request.vehicleId());
            return new ResourceNotFoundException("Vehicle not found");
        });

        if (!vehicle.getDriver().getId().equals(driver.getId())) {
            log.warn("Vehicle:{} Don't Belong To Driver:{}", vehicle.getDriver().getId(), driver.getId());
            throw new BusinessException("This vehicle does not belong to this driver");
        }

        validateSeats(request.seatsOffered(), vehicle.getCapacity());

        stoppingPointService.validatePoints(request.stoppingPoints());

        Trip trip = tripMapper.toEntity(request);

        trip.setDriver(driver);
        trip.setVehicle(vehicle);
        trip.setDepartureCity(request.stoppingPoints().get(0).city());
        trip.setDestinationCity(
                request.stoppingPoints()
                        .get(request.stoppingPoints().size() - 1).city());
        trip.setDepartureDate(
                request.stoppingPoints().get(0).passingTime());
        trip.setStatus(TripStatus.PUBLISHED);

        trip.getStoppingPoints().forEach(point -> point.setTrip(trip));

        return tripMapper.toResponseDTO(tripRepository.save(trip));
    }

    //Done
    @Override
    @Transactional(readOnly = true)
    public TripResponseDTO findById(Long id) {
    log.info("Getting Trip By ID: {}", id);
        Trip trip = tripRepository.findById(id).orElseThrow(() -> {
            log.warn("Trip: {}, Can't Be Found", id);
            return new ResourceNotFoundException("Trip not found");
        });

        return tripMapper.toResponseDTO(trip);
    }

    //Done
    @Override
    @Transactional(readOnly = true)
    public Page<TripResponseDTO> findAll(Pageable pageable) {
    log.info("Getting All Trips");
        return tripRepository.findAll(pageable)
                .map(tripMapper::toResponseDTO);
    }

    //Done
    @Override
    @Transactional(readOnly = true)
    public Page<TripResponseDTO> findByDriverId(Long driverId, Pageable pageable) {
    log.info("Getting Trip By It's Driver ID: {}", driverId);
        if (!driverRepository.existsById(driverId)) {
            throw new ResourceNotFoundException("Driver not found");
        }

        return tripRepository.findByDriverId(driverId, pageable)
                .map(tripMapper::toResponseDTO);
    }

    //Done
    @Override
    @Transactional(readOnly = true)
    public Page<TripResponseDTO> search(
            String departureCity,
            String destinationCity,
            LocalDate date,
            Integer numberOfSeats,
            BigDecimal maxPrice,
            Pageable pageable) {
    log.info("Searching for Trip");

        if (numberOfSeats != null && numberOfSeats < 1) {
            log.warn("Number is Seats is < 1");
            throw new BusinessException("Invalid number of seats");
        }

        if (maxPrice != null && maxPrice.signum() < 0) {
            log.warn("Maximum price is negative");
            throw new BusinessException("Maximum price cannot be negative");
        }

        return tripRepository.findByDepartureCityContainingIgnoreCaseOrDestinationCityContainingIgnoreCaseOrDepartureDateContainingIgnoreCaseOrSeatsOfferedContainingIgnoreCase(
                departureCity,
                destinationCity,
                date,
                numberOfSeats,
                pageable
        ).map(tripMapper::toResponseDTO);
    }

    //Done
    @Override
    @Transactional
    public TripResponseDTO update(Long tripId, TripRequestDTO request) {
    log.info("Updating Trip: {}", tripId);
        Trip trip = tripRepository.findById(tripId).orElseThrow(() -> {
            log.warn("Trip:{} is Not Found ", tripId);
            return new ResourceNotFoundException("Trip not found");
        });

        if (trip.getStatus() != TripStatus.PUBLISHED) {
            log.warn("Trip is Not PUBLISHED to Be Updated");
            throw new BusinessException("Only published trips can be updated");
        }

        if (!trip.getDriver().getId().equals(request.driverId())) {
            log.warn("Driver:{} Don't Own this Trip:{}", request.driverId(), trip.getId());
            throw new BusinessException("You are not allowed to update this trip");
        }

        if (bookingRepository.existsByTripId(tripId)) {
            log.warn("Trip With Booking Can't Be Updated");
            throw new BusinessException("Cannot update a trip that has bookings");
        }

        Vehicle vehicle = vehicleRepository.findById(request.vehicleId()).orElseThrow(() -> {
            log.warn("Vehicle: {}, Can't Be Found", request.vehicleId());
            return new ResourceNotFoundException("Vehicle not found");
        });

        if (!vehicle.getDriver().getId().equals(request.driverId())) {
            log.warn("Driver:{} Don't Own this Vehicle:{}", request.driverId(), vehicle.getId());
            throw new BusinessException("This vehicle does not belong to this driver"
            );
        }

        validateSeats(request.seatsOffered(), vehicle.getCapacity());

        stoppingPointService.validatePoints(request.stoppingPoints());

        trip.getStoppingPoints().clear();

        tripRepository.saveAndFlush(trip);

        Trip updatedTrip = tripMapper.toEntity(request);

        List<StoppingPoint> newPoints = updatedTrip.getStoppingPoints();

        newPoints.forEach(point -> point.setTrip(trip));

        trip.getStoppingPoints().addAll(newPoints);

        trip.setVehicle(vehicle);
        trip.setSeatsOffered(request.seatsOffered());

        trip.setDepartureCity(
                newPoints.get(0).getCity()
        );

        trip.setDestinationCity(
                newPoints.get(newPoints.size() - 1).getCity()
        );

        trip.setDepartureDate(
                newPoints.get(0).getPassingTime()
        );

        Trip savedTrip = tripRepository.save(trip);

        return tripMapper.toResponseDTO(savedTrip);
    }

    //Done
    @Override
    public TripResponseDTO complete(Long tripId, Long driverId) {
    log.info("Setting Trip Status To Complete");

        Trip trip = tripRepository.findById(tripId).orElseThrow(() -> {
            log.warn("Trip: {}, Can't Be Found", tripId);
            return new ResourceNotFoundException("Trip not found");
        });

        if (!trip.getDriver().getId().equals(driverId)) {
            log.warn("Driver: {} don't own this Trip: {}",driverId, trip.getId() );
            throw new BusinessException("You are not the driver of this trip");
        }

        if (trip.getStatus() != TripStatus.PUBLISHED && trip.getStatus() != TripStatus.FULL) {
            log.warn("Trip is Full or Canceled and it Can not be Completed");
            throw new BusinessException("This trip cannot be completed");
        }

        if (LocalDateTime.now().isBefore(trip.getDepartureDate())) {
            log.warn("Trip: {}, Can't Be Completed beFore Departure", trip.getId());
            throw new BusinessException("You cannot complete a trip before departure");
        }

        trip.setStatus(TripStatus.COMPLETED);

        return tripMapper.toResponseDTO(tripRepository.save(trip));
    }

    //Done
    @Override
    public TripResponseDTO cancel(Long tripId, Long driverId) {
    log.info("Canceling Trip: {}", tripId);

        Trip trip = tripRepository.findById(tripId).orElseThrow(() -> {
            log.warn("Trip: {}, Can't Be Found", tripId);
            return new ResourceNotFoundException("Trip not found");
        });

        if (!trip.getDriver().getId().equals(driverId)) {
            log.warn("Driver: {}, Don't Own this Trip: {}", driverId, trip.getId());
            throw new BusinessException("You are not the driver of this trip");
        }

        if (trip.getStatus() != TripStatus.PUBLISHED && trip.getStatus() != TripStatus.FULL) {
            log.warn("Can't Cancel Trip That is Completed");
            throw new BusinessException("This trip cannot be cancelled");
        }

        List<Booking> bookings = bookingRepository.findByTripIdAndStatus(tripId, BookingStatus.CONFIRMED);

        bookings.forEach(booking -> booking.setStatus(BookingStatus.CANCELLED));

        trip.setStatus(TripStatus.CANCELLED);

        return tripMapper.toResponseDTO(tripRepository.save(trip));
    }

    //done
    @Override
    @Transactional(readOnly = true)
    public int getRemainingSeats(Long tripId) {
    log.info("Getting Remaining Seats");
        Trip trip = tripRepository.findById(tripId).orElseThrow(() -> {
            log.warn("Trip: {}, Can't Be Found", tripId);
            return new ResourceNotFoundException("Trip not found");
        });

        Long bookedSeats = bookingRepository.sumConfirmedSeatsByTripId(tripId, BookingStatus.CONFIRMED);

        return trip.getSeatsOffered() - bookedSeats.intValue();
    }

    //Done
    private void validateSeats(int seatsOffered, int capacity) {
    log.info("Validating Seats");
        if (seatsOffered < 1 || seatsOffered > capacity) {
            log.warn("Seats Are Not > 1 Or More Than Vehicle Capacity");
            throw new BusinessException("Seats offered must be between 1 and vehicle capacity");
        }
    }
}
