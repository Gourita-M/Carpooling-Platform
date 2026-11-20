package org.carpooling.platform.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.carpooling.platform.exception.BusinessException;
import org.carpooling.platform.exception.ResourceNotFoundException;
import org.carpooling.platform.service.BookingService;
import org.carpooling.platform.service.TripService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.carpooling.platform.dto.request.BookingRequestDTO;
import org.carpooling.platform.dto.response.BookingResponseDTO;
import org.carpooling.platform.entity.Booking;
import org.carpooling.platform.entity.Passenger;
import org.carpooling.platform.entity.StoppingPoint;
import org.carpooling.platform.entity.Trip;
import org.carpooling.platform.enums.BookingStatus;
import org.carpooling.platform.enums.TripStatus;
import org.carpooling.platform.mapper.BookingMapper;
import org.carpooling.platform.repository.BookingRepository;
import org.carpooling.platform.repository.PassengerRepository;
import org.carpooling.platform.repository.StoppingPointRepository;
import org.carpooling.platform.repository.TripRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final PassengerRepository passengerRepository;
    private final TripRepository tripRepository;
    private final StoppingPointRepository stoppingPointRepository;
    private final TripService tripService;
    private final BookingMapper bookingMapper;


    @Override
    public BookingResponseDTO create(BookingRequestDTO request) {

        log.info("Creating Booking For Passenger ID: {}", request.passengerId());
        Passenger passenger = passengerRepository
                .findById(request.passengerId())
                .orElseThrow(() -> {
                    log.warn("Creating Booking Rejected, Passenger ID: {} is not Found", request.passengerId());
                    return new ResourceNotFoundException("Passenger not found");
                });

        Trip trip = tripRepository.findById(request.tripId())
                .orElseThrow(() -> {
                    log.warn("Creating Booking Rejected, Trip ID: {} is Not Found", request.tripId());
                    return new ResourceNotFoundException("Trip not found");
                });

        StoppingPoint pickUp = stoppingPointRepository
                .findById(request.pickUpPointId())
                .orElseThrow(() -> {
                    log.warn("Creating Booking Rejected, Pick up Point ID: {} is Not Found", request.pickUpPointId());
                    return new ResourceNotFoundException("Pickup point not found");
                });

        StoppingPoint dropOff = stoppingPointRepository
                .findById(request.dropOffPointId())
                .orElseThrow(() -> {
                    log.warn("Creating Booking Rejected, Drop off Point ID: {} is Not Found", request.dropOffPointId());
                    return new ResourceNotFoundException("Drop-off point not found");
                });

        if (trip.getStatus() != TripStatus.PUBLISHED) {
            log.warn("Creating Booking Rejected, Trip ID: {} is not available for booking", trip.getId());
            throw new BusinessException("This trip is not available for booking");
        }

        if (!pickUp.getTrip().getId().equals(trip.getId()) || !dropOff.getTrip().getId().equals(trip.getId())) {
            log.warn("Creating Booking Rejected, Stopping points don't belong to this trip");
            throw new BusinessException("Stopping points do not belong to this trip");
        }

        if (pickUp.getPointOrder() >= dropOff.getPointOrder()) {
            log.warn("Creating Booking Rejected, Pickup point come before drop-off point");
            throw new BusinessException("Pickup point must come before drop-off point");
        }

        if (request.numberOfSeats() < 1) {
            log.warn("Creating Booking Rejected, Number of Seats is < 1");
            throw new BusinessException("You must book at least one seat");
        }

        if (bookingRepository.existsByPassengerIdAndTripIdAndStatus(passenger.getId(), trip.getId(), BookingStatus.CONFIRMED)) {
            log.warn("Creating Booking Rejected, This Trip is Confirmed Already");
            throw new BusinessException("You already have a confirmed booking for this trip");
        }

        int remainingSeats = tripService.getRemainingSeats(trip.getId());

        if (request.numberOfSeats() > remainingSeats) {
            log.warn("Creating Booking Rejected, No Enough Seats Available");
            throw new BusinessException("Not enough seats available");
        }

        BigDecimal totalAmount = calculatePrice(pickUp.getId(), dropOff.getId(), request.numberOfSeats()
        );

        Booking booking = new Booking();

        booking.setPassenger(passenger);
        booking.setTrip(trip);
        booking.setPickUpPoint(pickUp);
        booking.setDropOffPoint(dropOff);
        booking.setNumberOfSeats(request.numberOfSeats());
        booking.setTotalAmount(totalAmount);
        booking.setBookingDate(LocalDateTime.now());
        booking.setStatus(BookingStatus.CONFIRMED);

        Booking savedBooking = bookingRepository.save(booking);

        if (remainingSeats - request.numberOfSeats() == 0) {
            trip.setStatus(TripStatus.FULL);
        }

        return bookingMapper.toResponseDTO(savedBooking);
    }
    //done
    @Override
    @Transactional(readOnly = true)
    public BookingResponseDTO findById(Long id) {
    log.info("Finding Booking With it's ID: {}", id);
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Booking ID: {}, is not Found", id);
                    return new ResourceNotFoundException("Booking not found");
                });

        return bookingMapper.toResponseDTO(booking);
    }
    //done
    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponseDTO> findByPassengerId(Long passengerId, Pageable pageable) {
    log.info("Finding a Booking By It's Passenger ID: {}", passengerId);

        if (!passengerRepository.existsById(passengerId)) {
            log.warn("Booking for Passenger ID: {}, is not Found", passengerId);
            throw new ResourceNotFoundException("Passenger not found");
        }

        return bookingRepository.findByPassengerId(passengerId, pageable)
                .map(bookingMapper::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponseDTO> findByTripId(Long tripId, Pageable pageable) {
        log.info("Finding Trip ID: {}", tripId);

        if (!tripRepository.existsById(tripId)) {
            log.warn("Trip ID: {}, is not Found", tripId);
            throw new ResourceNotFoundException("Trip not found");
        }

        return bookingRepository.findByTripId(tripId, pageable)
                .map(bookingMapper::toResponseDTO);
    }

    @Override
    public BookingResponseDTO cancel(Long bookingId, Long passengerId) {
    log.info("Canceling a Booking");

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.warn("Booking ID: {}, Can't Be Found To be Canceled", bookingId);
                    return new ResourceNotFoundException("Booking not found");
                });

        if (!booking.getPassenger().getId().equals(passengerId)) {
            log.warn("Booking ID: {}, Don't Belong To Passenger ID: {}", bookingId, passengerId);
            throw new BusinessException("You cannot cancel another passenger's booking");
        }

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            log.warn("Booking ID: {}, Already Cancelled", bookingId);
            throw new BusinessException("This booking is already cancelled");
        }

        Trip trip = booking.getTrip();

        LocalDateTime cancellationLimit =
                trip.getDepartureDate().minusHours(24);

        if (LocalDateTime.now().isAfter(cancellationLimit)) {
            log.warn("Booking ID: {}, Not Allowed to Be Cancelled 24H before Departure", bookingId);
            throw new BusinessException("Booking cancellation is only allowed 24 hours before departure");
        }

        booking.setStatus(BookingStatus.CANCELLED);

        if (trip.getStatus() == TripStatus.FULL) {
            trip.setStatus(TripStatus.PUBLISHED);
        }

        bookingRepository.save(booking);

        return bookingMapper.toResponseDTO(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculatePrice(Long pickUpPointId, Long dropOffPointId, int numberOfSeats) {
    log.info("Calculating Price");

        if (numberOfSeats < 1) {
            log.warn("Number of seats is Lower Than 1");
            throw new BusinessException("Number of seats must be at least 1");
        }

        StoppingPoint pickUp = stoppingPointRepository
                .findById(pickUpPointId)
                .orElseThrow(() -> {
                    log.warn("Pickup point ID: {}, Can't Be Found", pickUpPointId);
                    return new ResourceNotFoundException("Pickup point not found");
                });

        StoppingPoint dropOff = stoppingPointRepository
                .findById(dropOffPointId)
                .orElseThrow(() -> {
                    log.warn("Drop off ID: {}, Can't Be Found", dropOffPointId);
                    return new ResourceNotFoundException("Drop-off point not found");
                });

        if (!pickUp.getTrip().getId().equals(dropOff.getTrip().getId())) {
            log.warn("stopping points must belong to the same trip ID: {}", pickUp.getTrip().getId());
            throw new BusinessException("Both stopping points must belong to the same trip");
        }

        if (pickUp.getPointOrder() >= dropOff.getPointOrder()) {
            log.warn("Pickup point Can't be After drop-off point");
            throw new BusinessException("Pickup point must come before drop-off point");
        }

        BigDecimal pricePerSeat = dropOff.getPriceFromDeparture().subtract(pickUp.getPriceFromDeparture());

        return pricePerSeat.multiply(BigDecimal.valueOf(numberOfSeats)).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    @Transactional(readOnly = true)
    public int getRemainingSeats(Long tripId) {
        log.info("Getting RemainingSeats");
        return tripService.getRemainingSeats(tripId);
    }
}