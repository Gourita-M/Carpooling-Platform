
package org.carpooling.platform.service.impl;

import lombok.RequiredArgsConstructor;
import org.carpooling.platform.dto.request.TripRequestDTO;
import org.carpooling.platform.dto.request.StoppingPointRequestDTO;
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
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;
    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final BookingRepository bookingRepository;
    private final TripMapper tripMapper;
    private final StoppingPointService stoppingPointService;

    //Done PostMan
    @Override
    public TripResponseDTO create(TripRequestDTO request) {

        Driver driver = driverRepository.findById(request.driverId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Driver not found"));

        Vehicle vehicle = vehicleRepository.findById(request.vehicleId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Vehicle not found"));

        if (!vehicle.getDriver().getId().equals(driver.getId())) {
            throw new BusinessException(
                    "This vehicle does not belong to this driver");
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

    //Done PostMan
    @Override
    @Transactional(readOnly = true)
    public TripResponseDTO findById(Long id) {

        Trip trip = tripRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Trip not found"));

        return tripMapper.toResponseDTO(trip);
    }

    //Done PostMan
    @Override
    @Transactional(readOnly = true)
    public Page<TripResponseDTO> findAll(Pageable pageable) {

        return tripRepository.findAll(pageable)
                .map(tripMapper::toResponseDTO);
    }


    @Override
    @Transactional(readOnly = true)
    public Page<TripResponseDTO> findByDriverId(
            Long driverId, Pageable pageable) {

        if (!driverRepository.existsById(driverId)) {
            throw new ResourceNotFoundException("Driver not found");
        }

        return tripRepository.findByDriverId(driverId, pageable)
                .map(tripMapper::toResponseDTO);
    }

    //done
    @Override
    @Transactional(readOnly = true)
    public Page<TripResponseDTO> search(
            String departureCity,
            String destinationCity,
            LocalDate date,
            Integer numberOfSeats,
            BigDecimal maxPrice,
            Pageable pageable) {

        if (numberOfSeats != null && numberOfSeats < 1) {
            throw new BusinessException("Invalid number of seats");
        }

        if (maxPrice != null && maxPrice.signum() < 0) {
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

    @Override
    public TripResponseDTO update(Long tripId, TripRequestDTO request) {

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Trip not found"));

        if (trip.getStatus() != TripStatus.PUBLISHED) {
            throw new BusinessException(
                    "Only published trips can be updated");
        }

        if (!trip.getDriver().getId().equals(request.driverId())) {
            throw new BusinessException(
                    "You cannot change the driver of this trip");
        }

        if (bookingRepository.existsByTripId(tripId)) {
            throw new BusinessException(
                    "Cannot update a trip with booking history");
        }

        Vehicle vehicle = vehicleRepository.findById(request.vehicleId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Vehicle not found"));

        if (!vehicle.getDriver().getId().equals(request.driverId())) {
            throw new BusinessException(
                    "This vehicle does not belong to this driver");
        }

        validateSeats(request.seatsOffered(), vehicle.getCapacity());

        stoppingPointService.validatePoints(request.stoppingPoints());

        Trip updatedTrip = tripMapper.toEntity(request);

        trip.getStoppingPoints().clear();

        List<StoppingPoint> newPoints = updatedTrip.getStoppingPoints();

        newPoints.forEach(point -> point.setTrip(trip));
        trip.getStoppingPoints().addAll(newPoints);

        trip.setVehicle(vehicle);
        trip.setSeatsOffered(request.seatsOffered());
        trip.setDepartureCity(newPoints.get(0).getCity());
        trip.setDestinationCity(
                newPoints.get(newPoints.size() - 1).getCity());
        trip.setDepartureDate(newPoints.get(0).getPassingTime());

        return tripMapper.toResponseDTO(tripRepository.save(trip));
    }

    @Override
    public TripResponseDTO complete(Long tripId, Long driverId) {

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Trip not found"));

        if (!trip.getDriver().getId().equals(driverId)) {
            throw new BusinessException("You are not the driver of this trip");
        }

        if (trip.getStatus() != TripStatus.PUBLISHED &&
                trip.getStatus() != TripStatus.FULL) {
            throw new BusinessException("This trip cannot be completed");
        }

        if (LocalDateTime.now().isBefore(trip.getDepartureDate())) {
            throw new BusinessException(
                    "You cannot complete a trip before departure");
        }

        trip.setStatus(TripStatus.COMPLETED);

        return tripMapper.toResponseDTO(tripRepository.save(trip));
    }

    @Override
    public TripResponseDTO cancel(Long tripId, Long driverId) {

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Trip not found"));

        if (!trip.getDriver().getId().equals(driverId)) {
            throw new BusinessException("You are not the driver of this trip");
        }

        if (trip.getStatus() != TripStatus.PUBLISHED &&
                trip.getStatus() != TripStatus.FULL) {
            throw new BusinessException("This trip cannot be cancelled");
        }

        List<Booking> bookings =
                bookingRepository.findByTripIdAndStatus(
                        tripId, BookingStatus.CONFIRMED);

        bookings.forEach(booking ->
                booking.setStatus(BookingStatus.CANCELLED));

        trip.setStatus(TripStatus.CANCELLED);

        return tripMapper.toResponseDTO(tripRepository.save(trip));
    }

    @Override
    @Transactional(readOnly = true)
    public int getRemainingSeats(Long tripId) {

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Trip not found"));

        Long bookedSeats = bookingRepository.sumConfirmedSeatsByTripId(
                tripId, BookingStatus.CONFIRMED);

        return trip.getSeatsOffered() - bookedSeats.intValue();
    }

    private void validateSeats(int seatsOffered, int capacity) {

        if (seatsOffered < 1 || seatsOffered > capacity) {
            throw new BusinessException(
                    "Seats offered must be between 1 and vehicle capacity");
        }
    }
}
