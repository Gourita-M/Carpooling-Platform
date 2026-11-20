package org.carpooling.platform.service.impl;

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
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final PassengerRepository passengerRepository;
    private final TripRepository tripRepository;
    private final StoppingPointRepository stoppingPointRepository;
    private final TripService tripService;
    private final BookingMapper bookingMapper;


    @Override
    public BookingResponseDTO create(BookingRequestDTO request) {

        Passenger passenger = passengerRepository
                .findById(request.passengerId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Passenger not found"));

        Trip trip = tripRepository.findById(request.tripId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Trip not found"));

        StoppingPoint pickUp = stoppingPointRepository
                .findById(request.pickUpPointId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Pickup point not found"));

        StoppingPoint dropOff = stoppingPointRepository
                .findById(request.dropOffPointId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Drop-off point not found"));

        if (trip.getStatus() != TripStatus.PUBLISHED) {
            throw new BusinessException(
                    "This trip is not available for booking");
        }

        if (!pickUp.getTrip().getId().equals(trip.getId()) ||
                !dropOff.getTrip().getId().equals(trip.getId())) {
            throw new BusinessException(
                    "Stopping points do not belong to this trip");
        }

        if (pickUp.getPointOrder() >= dropOff.getPointOrder()) {
            throw new BusinessException(
                    "Pickup point must come before drop-off point");
        }

        if (request.numberOfSeats() < 1) {
            throw new BusinessException(
                    "You must book at least one seat");
        }

        if (bookingRepository.existsByPassengerIdAndTripIdAndStatus(
                passenger.getId(),
                trip.getId(),
                BookingStatus.CONFIRMED)) {

            throw new BusinessException(
                    "You already have a confirmed booking for this trip");
        }

        int remainingSeats = tripService.getRemainingSeats(trip.getId());

        if (request.numberOfSeats() > remainingSeats) {
            throw new BusinessException("Not enough seats available");
        }

        BigDecimal totalAmount = calculatePrice(
                pickUp.getId(),
                dropOff.getId(),
                request.numberOfSeats()
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

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Booking not found"));

        return bookingMapper.toResponseDTO(booking);
    }
    //done
    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponseDTO> findByPassengerId(
            Long passengerId, Pageable pageable) {

        if (!passengerRepository.existsById(passengerId)) {
            throw new ResourceNotFoundException("Passenger not found");
        }

        return bookingRepository.findByPassengerId(passengerId, pageable)
                .map(bookingMapper::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponseDTO> findByTripId(
            Long tripId, Pageable pageable) {

        if (!tripRepository.existsById(tripId)) {
            throw new ResourceNotFoundException("Trip not found");
        }

        return bookingRepository.findByTripId(tripId, pageable)
                .map(bookingMapper::toResponseDTO);
    }

    @Override
    public BookingResponseDTO cancel(Long bookingId, Long passengerId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Booking not found"));

        if (!booking.getPassenger().getId().equals(passengerId)) {
            throw new BusinessException(
                    "You cannot cancel another passenger's booking");
        }

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BusinessException(
                    "This booking is already cancelled");
        }

        Trip trip = booking.getTrip();

        LocalDateTime cancellationLimit =
                trip.getDepartureDate().minusHours(24);

        if (LocalDateTime.now().isAfter(cancellationLimit)) {
            throw new BusinessException(
                    "Booking cancellation is only allowed 24 hours before departure");
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
    public BigDecimal calculatePrice(
            Long pickUpPointId,
            Long dropOffPointId,
            int numberOfSeats) {

        if (numberOfSeats < 1) {
            throw new BusinessException(
                    "Number of seats must be at least 1");
        }

        StoppingPoint pickUp = stoppingPointRepository
                .findById(pickUpPointId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Pickup point not found"));

        StoppingPoint dropOff = stoppingPointRepository
                .findById(dropOffPointId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Drop-off point not found"));

        if (!pickUp.getTrip().getId().equals(dropOff.getTrip().getId())) {
            throw new BusinessException(
                    "Both stopping points must belong to the same trip");
        }

        if (pickUp.getPointOrder() >= dropOff.getPointOrder()) {
            throw new BusinessException(
                    "Pickup point must come before drop-off point");
        }

        BigDecimal pricePerSeat =
                dropOff.getPriceFromDeparture()
                        .subtract(pickUp.getPriceFromDeparture());

        return pricePerSeat
                .multiply(BigDecimal.valueOf(numberOfSeats))
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    @Transactional(readOnly = true)
    public int getRemainingSeats(Long tripId) {
        return tripService.getRemainingSeats(tripId);
    }
}