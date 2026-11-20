
package org.carpooling.platform.service.impl;

import org.carpooling.platform.dto.request.BookingRequestDTO;
import org.carpooling.platform.dto.response.BookingResponseDTO;
import org.carpooling.platform.entity.Booking;
import org.carpooling.platform.entity.Passenger;
import org.carpooling.platform.entity.StoppingPoint;
import org.carpooling.platform.entity.Trip;
import org.carpooling.platform.enums.BookingStatus;
import org.carpooling.platform.enums.TripStatus;
import org.carpooling.platform.exception.BusinessException;
import org.carpooling.platform.exception.ResourceNotFoundException;
import org.carpooling.platform.mapper.BookingMapper;
import org.carpooling.platform.repository.BookingRepository;
import org.carpooling.platform.repository.PassengerRepository;
import org.carpooling.platform.repository.StoppingPointRepository;
import org.carpooling.platform.repository.TripRepository;
import org.carpooling.platform.service.TripService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private PassengerRepository passengerRepository;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private StoppingPointRepository stoppingPointRepository;

    @Mock
    private TripService tripService;

    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private Passenger passenger;
    private Trip trip;
    private StoppingPoint pickUp;
    private StoppingPoint dropOff;
    private Booking booking;
    private BookingResponseDTO response;

    @BeforeEach
    void setUp() {
        passenger = new Passenger();
        passenger.setId(1L);

        trip = new Trip();
        trip.setId(10L);
        trip.setStatus(TripStatus.PUBLISHED);
        trip.setDepartureDate(LocalDateTime.now().plusDays(3));

        pickUp = new StoppingPoint();
        pickUp.setId(100L);
        pickUp.setTrip(trip);
        pickUp.setPointOrder(0);
        pickUp.setPriceFromDeparture(BigDecimal.ZERO);

        dropOff = new StoppingPoint();
        dropOff.setId(101L);
        dropOff.setTrip(trip);
        dropOff.setPointOrder(1);
        dropOff.setPriceFromDeparture(new BigDecimal("50.00"));

        booking = new Booking();
        booking.setId(20L);
        booking.setPassenger(passenger);
        booking.setTrip(trip);
        booking.setPickUpPoint(pickUp);
        booking.setDropOffPoint(dropOff);
        booking.setNumberOfSeats(2);
        booking.setTotalAmount(new BigDecimal("100.00"));
        booking.setStatus(BookingStatus.CONFIRMED);

        response = mock(BookingResponseDTO.class);
    }

    @Test
    void create_shouldCreateBookingSuccessfully() {
        BookingRequestDTO request = request(2);

        when(passengerRepository.findById(1L))
                .thenReturn(Optional.of(passenger));
        when(tripRepository.findById(10L))
                .thenReturn(Optional.of(trip));
        when(stoppingPointRepository.findById(100L))
                .thenReturn(Optional.of(pickUp));
        when(stoppingPointRepository.findById(101L))
                .thenReturn(Optional.of(dropOff));
        when(bookingRepository.existsByPassengerIdAndTripIdAndStatus(
                1L, 10L, BookingStatus.CONFIRMED))
                .thenReturn(false);
        when(tripService.getRemainingSeats(10L)).thenReturn(4);
        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(bookingMapper.toResponseDTO(any(Booking.class)))
                .thenReturn(response);

        BookingResponseDTO result = bookingService.create(request);

        assertThat(result).isEqualTo(response);
        assertThat(trip.getStatus()).isEqualTo(TripStatus.PUBLISHED);

        verify(bookingRepository).save(argThat(saved ->
                saved.getPassenger().equals(passenger)
                        && saved.getTrip().equals(trip)
                        && saved.getNumberOfSeats() == 2
                        && saved.getTotalAmount()
                        .compareTo(new BigDecimal("100.00")) == 0
                        && saved.getStatus() == BookingStatus.CONFIRMED
                        && saved.getBookingDate() != null
        ));
    }

    @Test
    void create_shouldSetTripStatusToFull_whenAllSeatsAreBooked() {
        when(passengerRepository.findById(1L))
                .thenReturn(Optional.of(passenger));
        when(tripRepository.findById(10L))
                .thenReturn(Optional.of(trip));
        when(stoppingPointRepository.findById(100L))
                .thenReturn(Optional.of(pickUp));
        when(stoppingPointRepository.findById(101L))
                .thenReturn(Optional.of(dropOff));
        when(bookingRepository.existsByPassengerIdAndTripIdAndStatus(
                1L, 10L, BookingStatus.CONFIRMED))
                .thenReturn(false);
        when(tripService.getRemainingSeats(10L)).thenReturn(2);
        when(bookingRepository.save(any(Booking.class)))
                .thenReturn(booking);
        when(bookingMapper.toResponseDTO(any(Booking.class)))
                .thenReturn(response);

        bookingService.create(request(2));

        assertThat(trip.getStatus()).isEqualTo(TripStatus.FULL);
    }

    @Test
    void create_shouldThrowException_whenPassengerDoesNotExist() {
        when(passengerRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.create(request(2)))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(tripRepository, stoppingPointRepository);
    }

    @Test
    void create_shouldThrowException_whenTripDoesNotExist() {
        when(passengerRepository.findById(1L))
                .thenReturn(Optional.of(passenger));
        when(tripRepository.findById(10L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.create(request(2)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_shouldThrowException_whenPickupPointDoesNotExist() {
        when(passengerRepository.findById(1L))
                .thenReturn(Optional.of(passenger));
        when(tripRepository.findById(10L))
                .thenReturn(Optional.of(trip));
        when(stoppingPointRepository.findById(100L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.create(request(2)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_shouldThrowException_whenTripIsNotPublished() {
        trip.setStatus(TripStatus.FULL);

        mockCreateDependencies();

        assertThatThrownBy(() -> bookingService.create(request(2)))
                .isInstanceOf(BusinessException.class);

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowException_whenPointsBelongToAnotherTrip() {
        Trip anotherTrip = new Trip();
        anotherTrip.setId(99L);
        dropOff.setTrip(anotherTrip);

        mockCreateDependencies();

        assertThatThrownBy(() -> bookingService.create(request(2)))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void create_shouldThrowException_whenPickupIsAfterDropoff() {
        pickUp.setPointOrder(2);
        dropOff.setPointOrder(1);

        mockCreateDependencies();

        assertThatThrownBy(() -> bookingService.create(request(2)))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void create_shouldThrowException_whenNumberOfSeatsIsZero() {
        mockCreateDependencies();

        assertThatThrownBy(() -> bookingService.create(request(0)))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void create_shouldThrowException_whenPassengerAlreadyHasConfirmedBooking() {
        mockCreateDependencies();

        when(bookingRepository.existsByPassengerIdAndTripIdAndStatus(
                1L, 10L, BookingStatus.CONFIRMED))
                .thenReturn(true);

        assertThatThrownBy(() -> bookingService.create(request(2)))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void create_shouldThrowException_whenThereAreNotEnoughSeats() {
        mockCreateDependencies();

        when(bookingRepository.existsByPassengerIdAndTripIdAndStatus(
                1L, 10L, BookingStatus.CONFIRMED))
                .thenReturn(false);
        when(tripService.getRemainingSeats(10L)).thenReturn(1);

        assertThatThrownBy(() -> bookingService.create(request(2)))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void findById_shouldReturnBooking() {
        when(bookingRepository.findById(20L))
                .thenReturn(Optional.of(booking));
        when(bookingMapper.toResponseDTO(booking))
                .thenReturn(response);

        BookingResponseDTO result = bookingService.findById(20L);

        assertThat(result).isEqualTo(response);
    }

    @Test
    void findById_shouldThrowException_whenBookingDoesNotExist() {
        when(bookingRepository.findById(20L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.findById(20L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findByPassengerId_shouldReturnBookings() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Booking> bookings = new PageImpl<>(List.of(booking));

        when(passengerRepository.existsById(1L)).thenReturn(true);
        when(bookingRepository.findByPassengerId(1L, pageable))
                .thenReturn(bookings);
        when(bookingMapper.toResponseDTO(booking))
                .thenReturn(response);

        Page<BookingResponseDTO> result =
                bookingService.findByPassengerId(1L, pageable);

        assertThat(result.getContent()).containsExactly(response);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void findByPassengerId_shouldThrowException_whenPassengerDoesNotExist() {
        PageRequest pageable = PageRequest.of(0, 10);

        when(passengerRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() ->
                bookingService.findByPassengerId(1L, pageable))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(bookingRepository, never())
                .findByPassengerId(anyLong(), any());
    }

    @Test
    void findByTripId_shouldReturnBookings() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Booking> bookings = new PageImpl<>(List.of(booking));

        when(tripRepository.existsById(10L)).thenReturn(true);
        when(bookingRepository.findByTripId(10L, pageable))
                .thenReturn(bookings);
        when(bookingMapper.toResponseDTO(booking))
                .thenReturn(response);

        Page<BookingResponseDTO> result =
                bookingService.findByTripId(10L, pageable);

        assertThat(result.getContent()).containsExactly(response);
    }

    @Test
    void findByTripId_shouldThrowException_whenTripDoesNotExist() {
        PageRequest pageable = PageRequest.of(0, 10);

        when(tripRepository.existsById(10L)).thenReturn(false);

        assertThatThrownBy(() ->
                bookingService.findByTripId(10L, pageable))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(bookingRepository, never()).findByTripId(anyLong(), any());
    }

    @Test
    void cancel_shouldCancelBookingSuccessfully() {
        trip.setStatus(TripStatus.PUBLISHED);
        trip.setDepartureDate(LocalDateTime.now().plusDays(3));

        when(bookingRepository.findById(20L))
                .thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);
        when(bookingMapper.toResponseDTO(booking)).thenReturn(response);

        BookingResponseDTO result = bookingService.cancel(20L, 1L);

        assertThat(result).isEqualTo(response);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        verify(bookingRepository).save(booking);
    }

    @Test
    void cancel_shouldReopenTrip_whenTripWasFull() {
        trip.setStatus(TripStatus.FULL);
        trip.setDepartureDate(LocalDateTime.now().plusDays(3));

        when(bookingRepository.findById(20L))
                .thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);
        when(bookingMapper.toResponseDTO(booking)).thenReturn(response);

        bookingService.cancel(20L, 1L);

        assertThat(trip.getStatus()).isEqualTo(TripStatus.PUBLISHED);
    }

    @Test
    void cancel_shouldThrowException_whenBookingDoesNotExist() {
        when(bookingRepository.findById(20L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.cancel(20L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void cancel_shouldThrowException_whenPassengerDoesNotOwnBooking() {
        when(bookingRepository.findById(20L))
                .thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancel(20L, 999L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void cancel_shouldThrowException_whenBookingIsAlreadyCancelled() {
        booking.setStatus(BookingStatus.CANCELLED);

        when(bookingRepository.findById(20L))
                .thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancel(20L, 1L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void cancel_shouldThrowException_whenDepartureIsLessThan24HoursAway() {
        trip.setDepartureDate(LocalDateTime.now().plusHours(12));

        when(bookingRepository.findById(20L))
                .thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancel(20L, 1L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void calculatePrice_shouldReturnCorrectPrice() {
        when(stoppingPointRepository.findById(100L))
                .thenReturn(Optional.of(pickUp));
        when(stoppingPointRepository.findById(101L))
                .thenReturn(Optional.of(dropOff));

        BigDecimal result =
                bookingService.calculatePrice(100L, 101L, 3);

        assertThat(result).isEqualByComparingTo("150.00");
    }

    @Test
    void calculatePrice_shouldThrowException_whenNumberOfSeatsIsZero() {
        assertThatThrownBy(() ->
                bookingService.calculatePrice(100L, 101L, 0))
                .isInstanceOf(BusinessException.class);

        verifyNoInteractions(stoppingPointRepository);
    }

    @Test
    void calculatePrice_shouldThrowException_whenPickupDoesNotExist() {
        when(stoppingPointRepository.findById(100L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                bookingService.calculatePrice(100L, 101L, 2))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void calculatePrice_shouldThrowException_whenDropoffDoesNotExist() {
        when(stoppingPointRepository.findById(100L))
                .thenReturn(Optional.of(pickUp));
        when(stoppingPointRepository.findById(101L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                bookingService.calculatePrice(100L, 101L, 2))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void calculatePrice_shouldThrowException_whenPointsBelongToDifferentTrips() {
        Trip anotherTrip = new Trip();
        anotherTrip.setId(99L);
        dropOff.setTrip(anotherTrip);

        when(stoppingPointRepository.findById(100L))
                .thenReturn(Optional.of(pickUp));
        when(stoppingPointRepository.findById(101L))
                .thenReturn(Optional.of(dropOff));

        assertThatThrownBy(() ->
                bookingService.calculatePrice(100L, 101L, 2))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void calculatePrice_shouldThrowException_whenPickupIsAfterDropoff() {
        pickUp.setPointOrder(2);
        dropOff.setPointOrder(1);

        when(stoppingPointRepository.findById(100L))
                .thenReturn(Optional.of(pickUp));
        when(stoppingPointRepository.findById(101L))
                .thenReturn(Optional.of(dropOff));

        assertThatThrownBy(() ->
                bookingService.calculatePrice(100L, 101L, 2))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void getRemainingSeats_shouldDelegateToTripService() {
        when(tripService.getRemainingSeats(10L)).thenReturn(3);

        int result = bookingService.getRemainingSeats(10L);

        assertThat(result).isEqualTo(3);
        verify(tripService).getRemainingSeats(10L);
    }

    private BookingRequestDTO request(int seats) {
        return new BookingRequestDTO(
                1L,
                10L,
                100L,
                101L,
                seats
        );
    }

    private void mockCreateDependencies() {
        when(passengerRepository.findById(1L))
                .thenReturn(Optional.of(passenger));
        when(tripRepository.findById(10L))
                .thenReturn(Optional.of(trip));
        when(stoppingPointRepository.findById(100L))
                .thenReturn(Optional.of(pickUp));
        when(stoppingPointRepository.findById(101L))
                .thenReturn(Optional.of(dropOff));
    }
}
