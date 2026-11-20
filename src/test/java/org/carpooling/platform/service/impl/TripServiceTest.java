package org.carpooling.platform.service.impl;

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
import org.carpooling.platform.service.StoppingPointService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripServiceTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private TripMapper tripMapper;

    @Mock
    private StoppingPointService stoppingPointService;

    @InjectMocks
    private TripServiceImpl tripService;

    private TripRequestDTO request;
    private TripResponseDTO response;

    private Trip trip;
    private Trip mappedTrip;

    private Driver driver;
    private Vehicle vehicle;
    private StoppingPoint stoppingPoint;

    private List<StoppingPointRequestDTO> requestPoints;

    @BeforeEach
    void setUp() {
        request = mock(TripRequestDTO.class);
        response = mock(TripResponseDTO.class);

        trip = mock(Trip.class);
        mappedTrip = mock(Trip.class);

        driver = mock(Driver.class);
        vehicle = mock(Vehicle.class);
        stoppingPoint = mock(StoppingPoint.class);

        requestPoints = List.of(
                mock(StoppingPointRequestDTO.class),
                mock(StoppingPointRequestDTO.class)
        );

        when(request.driverId()).thenReturn(1L);
        when(request.vehicleId()).thenReturn(2L);
        when(request.seatsOffered()).thenReturn(3);
        when(request.stoppingPoints()).thenReturn(requestPoints);
    }

    // ==========================================
    // CREATE
    // ==========================================

    @Test
    void create_shouldCreateTripSuccessfully() {

        mockCreateDependencies();

        when(tripMapper.toEntity(request)).thenReturn(trip);

        when(trip.getStoppingPoints())
                .thenReturn(new ArrayList<>(List.of(stoppingPoint)));

        when(tripRepository.save(trip)).thenReturn(trip);
        when(tripMapper.toResponseDTO(trip)).thenReturn(response);

        TripResponseDTO result = tripService.create(request);

        assertThat(result).isEqualTo(response);

        verify(trip).setDriver(driver);
        verify(trip).setVehicle(vehicle);
        verify(trip).setDepartureCity(requestPoints.get(0).city());
        verify(trip).setDestinationCity(requestPoints.get(1).city());
        verify(trip).setDepartureDate(requestPoints.get(0).passingTime());
        verify(trip).setStatus(TripStatus.PUBLISHED);

        verify(stoppingPoint).setTrip(trip);
        verify(tripRepository).save(trip);
    }

    @Test
    void create_shouldThrowException_whenDriverDoesNotExist() {

        when(driverRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> tripService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Driver not found");

        verifyNoInteractions(vehicleRepository, tripMapper);
    }

    @Test
    void create_shouldThrowException_whenVehicleDoesNotExist() {

        when(driverRepository.findById(1L))
                .thenReturn(Optional.of(driver));

        when(vehicleRepository.findById(2L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> tripService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Vehicle not found");
    }

    @Test
    void create_shouldThrowException_whenVehicleDoesNotBelongToDriver() {

        Driver anotherDriver = mock(Driver.class);

        when(driverRepository.findById(1L))
                .thenReturn(Optional.of(driver));

        when(vehicleRepository.findById(2L))
                .thenReturn(Optional.of(vehicle));

        when(vehicle.getDriver()).thenReturn(anotherDriver);
        when(anotherDriver.getId()).thenReturn(99L);
        when(driver.getId()).thenReturn(1L);

        assertThatThrownBy(() -> tripService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("This vehicle does not belong to this driver");

        verifyNoInteractions(tripMapper);
    }

    @Test
    void create_shouldThrowException_whenSeatsOfferedExceedCapacity() {

        when(driverRepository.findById(1L))
                .thenReturn(Optional.of(driver));

        when(vehicleRepository.findById(2L))
                .thenReturn(Optional.of(vehicle));

        when(vehicle.getDriver()).thenReturn(driver);
        when(driver.getId()).thenReturn(1L);
        when(vehicle.getCapacity()).thenReturn(2);

        assertThatThrownBy(() -> tripService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(
                        "Seats offered must be between 1 and vehicle capacity");

        verifyNoInteractions(tripMapper);
    }

    @Test
    void create_shouldThrowException_whenSeatsOfferedAreZero() {

        when(request.seatsOffered()).thenReturn(0);

        when(driverRepository.findById(1L))
                .thenReturn(Optional.of(driver));

        when(vehicleRepository.findById(2L))
                .thenReturn(Optional.of(vehicle));

        when(vehicle.getDriver()).thenReturn(driver);
        when(driver.getId()).thenReturn(1L);
        when(vehicle.getCapacity()).thenReturn(4);

        assertThatThrownBy(() -> tripService.create(request))
                .isInstanceOf(BusinessException.class);

        verifyNoInteractions(tripMapper);
    }

    @Test
    void create_shouldValidateStoppingPoints() {

        mockCreateDependencies();

        when(tripMapper.toEntity(request)).thenReturn(trip);
        when(trip.getStoppingPoints()).thenReturn(new ArrayList<>());
        when(tripRepository.save(trip)).thenReturn(trip);
        when(tripMapper.toResponseDTO(trip)).thenReturn(response);

        tripService.create(request);

        verify(stoppingPointService).validatePoints(requestPoints);
    }

    // ==========================================
    // FIND BY ID
    // ==========================================

    @Test
    void findById_shouldReturnTripSuccessfully() {

        when(tripRepository.findById(10L))
                .thenReturn(Optional.of(trip));

        when(tripMapper.toResponseDTO(trip))
                .thenReturn(response);

        TripResponseDTO result = tripService.findById(10L);

        assertThat(result).isEqualTo(response);

        verify(tripRepository).findById(10L);
        verify(tripMapper).toResponseDTO(trip);
    }

    @Test
    void findById_shouldThrowException_whenTripDoesNotExist() {

        when(tripRepository.findById(10L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> tripService.findById(10L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Trip not found");

        verifyNoInteractions(tripMapper);
    }

    // ==========================================
    // FIND ALL
    // ==========================================

    @Test
    void findAll_shouldReturnTripsSuccessfully() {

        Pageable pageable = PageRequest.of(0, 10);

        Trip secondTrip = mock(Trip.class);
        TripResponseDTO secondResponse = mock(TripResponseDTO.class);

        Page<Trip> tripPage =
                new PageImpl<>(List.of(trip, secondTrip));

        when(tripRepository.findAll(pageable))
                .thenReturn(tripPage);

        when(tripMapper.toResponseDTO(trip))
                .thenReturn(response);

        when(tripMapper.toResponseDTO(secondTrip))
                .thenReturn(secondResponse);

        Page<TripResponseDTO> result =
                tripService.findAll(pageable);

        assertThat(result.getContent())
                .containsExactly(response, secondResponse);

        assertThat(result.getTotalElements()).isEqualTo(2);

        verify(tripRepository).findAll(pageable);
    }

    @Test
    void findAll_shouldReturnEmptyPage_whenNoTripsExist() {

        Pageable pageable = PageRequest.of(0, 10);

        when(tripRepository.findAll(pageable))
                .thenReturn(Page.empty(pageable));

        Page<TripResponseDTO> result =
                tripService.findAll(pageable);

        assertThat(result.getContent()).isEmpty();

        verifyNoInteractions(tripMapper);
    }

    // ==========================================
    // FIND BY DRIVER ID
    // ==========================================

    @Test
    void findByDriverId_shouldReturnDriversTrips() {

        Pageable pageable = PageRequest.of(0, 10);

        when(driverRepository.existsById(1L)).thenReturn(true);

        when(tripRepository.findByDriverId(1L, pageable))
                .thenReturn(new PageImpl<>(List.of(trip)));

        when(tripMapper.toResponseDTO(trip))
                .thenReturn(response);

        Page<TripResponseDTO> result =
                tripService.findByDriverId(1L, pageable);

        assertThat(result.getContent()).containsExactly(response);

        verify(driverRepository).existsById(1L);
        verify(tripRepository).findByDriverId(1L, pageable);
    }

    @Test
    void findByDriverId_shouldThrowException_whenDriverDoesNotExist() {

        Pageable pageable = PageRequest.of(0, 10);

        when(driverRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() ->
                tripService.findByDriverId(1L, pageable))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Driver not found");

        verify(tripRepository, never()).findByDriverId(any(), any());
    }

    // ==========================================
    // SEARCH
    // ==========================================

    @Test
    void search_shouldReturnMatchingTrips() {

        Pageable pageable = PageRequest.of(0, 10);

        LocalDate date = LocalDate.now();

        when(tripRepository
                .findByDepartureCityContainingIgnoreCaseOrDestinationCityContainingIgnoreCaseOrDepartureDateContainingIgnoreCaseOrSeatsOfferedContainingIgnoreCase(
                        "Safi",
                        "Casablanca",
                        date,
                        2,
                        pageable
                ))
                .thenReturn(new PageImpl<>(List.of(trip)));

        when(tripMapper.toResponseDTO(trip))
                .thenReturn(response);

        Page<TripResponseDTO> result = tripService.search(
                "Safi",
                "Casablanca",
                date,
                2,
                new BigDecimal("100.00"),
                pageable
        );

        assertThat(result.getContent()).containsExactly(response);
    }

    @Test
    void search_shouldThrowException_whenNumberOfSeatsIsZero() {

        Pageable pageable = PageRequest.of(0, 10);

        assertThatThrownBy(() -> tripService.search(
                "Safi",
                "Casablanca",
                LocalDate.now(),
                0,
                null,
                pageable
        ))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Invalid number of seats");

        verifyNoInteractions(tripRepository, tripMapper);
    }

    @Test
    void search_shouldThrowException_whenNumberOfSeatsIsNegative() {

        Pageable pageable = PageRequest.of(0, 10);

        assertThatThrownBy(() -> tripService.search(
                "Safi",
                "Casablanca",
                LocalDate.now(),
                -1,
                null,
                pageable
        ))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void search_shouldThrowException_whenMaximumPriceIsNegative() {

        Pageable pageable = PageRequest.of(0, 10);

        assertThatThrownBy(() -> tripService.search(
                "Safi",
                "Casablanca",
                LocalDate.now(),
                2,
                new BigDecimal("-10.00"),
                pageable
        ))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Maximum price cannot be negative");
    }

    // ==========================================
    // UPDATE
    // ==========================================

    @Test
    void update_shouldUpdateTripSuccessfully() {

        List<StoppingPoint> oldPoints = new ArrayList<>();
        List<StoppingPoint> newPoints = new ArrayList<>();

        StoppingPoint newPoint = mock(StoppingPoint.class);
        newPoints.add(newPoint);

        mockUpdateDependencies(oldPoints, newPoints);

        when(tripMapper.toEntity(request)).thenReturn(mappedTrip);
        when(mappedTrip.getStoppingPoints()).thenReturn(newPoints);

        when(tripRepository.saveAndFlush(trip)).thenReturn(trip);
        when(tripRepository.save(trip)).thenReturn(trip);
        when(tripMapper.toResponseDTO(trip)).thenReturn(response);

        TripResponseDTO result = tripService.update(10L, request);

        assertThat(result).isEqualTo(response);

        verify(tripRepository).saveAndFlush(trip);
        verify(newPoint).setTrip(trip);
        verify(tripRepository).save(trip);

        verify(trip).setVehicle(vehicle);
        verify(trip).setSeatsOffered(3);
    }

    @Test
    void update_shouldThrowException_whenTripDoesNotExist() {

        when(tripRepository.findById(10L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> tripService.update(10L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Trip not found");
    }

    @Test
    void update_shouldThrowException_whenTripIsNotPublished() {

        when(tripRepository.findById(10L))
                .thenReturn(Optional.of(trip));

        when(trip.getStatus()).thenReturn(TripStatus.COMPLETED);

        assertThatThrownBy(() -> tripService.update(10L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Only published trips can be updated");
    }

    @Test
    void update_shouldThrowException_whenDriverIsNotOwner() {

        when(tripRepository.findById(10L))
                .thenReturn(Optional.of(trip));

        when(trip.getStatus()).thenReturn(TripStatus.PUBLISHED);

        Driver tripDriver = mock(Driver.class);

        when(trip.getDriver()).thenReturn(tripDriver);
        when(tripDriver.getId()).thenReturn(99L);

        when(request.driverId()).thenReturn(1L);

        assertThatThrownBy(() -> tripService.update(10L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("You are not allowed to update this trip");
    }

    @Test
    void update_shouldThrowException_whenTripHasBookings() {

        Driver tripDriver = mock(Driver.class);

        when(tripRepository.findById(10L))
                .thenReturn(Optional.of(trip));

        when(trip.getStatus()).thenReturn(TripStatus.PUBLISHED);
        when(trip.getDriver()).thenReturn(tripDriver);
        when(tripDriver.getId()).thenReturn(1L);

        when(bookingRepository.existsByTripId(10L))
                .thenReturn(true);

        assertThatThrownBy(() -> tripService.update(10L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Cannot update a trip that has bookings");

        verifyNoInteractions(vehicleRepository);
    }

    @Test
    void update_shouldThrowException_whenVehicleDoesNotExist() {

        mockUpdateBeforeVehicleLookup();

        when(vehicleRepository.findById(2L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> tripService.update(10L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Vehicle not found");
    }

    @Test
    void update_shouldThrowException_whenVehicleBelongsToAnotherDriver() {

        Driver anotherDriver = mock(Driver.class);

        mockUpdateBeforeVehicleLookup();

        when(vehicleRepository.findById(2L))
                .thenReturn(Optional.of(vehicle));

        when(vehicle.getDriver()).thenReturn(anotherDriver);
        when(anotherDriver.getId()).thenReturn(99L);

        assertThatThrownBy(() -> tripService.update(10L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("This vehicle does not belong to this driver");
    }

    @Test
    void update_shouldThrowException_whenSeatsExceedVehicleCapacity() {

        mockUpdateBeforeVehicleLookup();

        when(vehicleRepository.findById(2L))
                .thenReturn(Optional.of(vehicle));

        when(vehicle.getDriver()).thenReturn(driver);
        when(driver.getId()).thenReturn(1L);
        when(vehicle.getCapacity()).thenReturn(2);

        assertThatThrownBy(() -> tripService.update(10L, request))
                .isInstanceOf(BusinessException.class);

        verifyNoInteractions(tripMapper);
    }

    // ==========================================
    // COMPLETE
    // ==========================================

    @Test
    void complete_shouldCompleteTripSuccessfully() {

        when(tripRepository.findById(10L))
                .thenReturn(Optional.of(trip));

        when(trip.getDriver()).thenReturn(driver);
        when(driver.getId()).thenReturn(1L);

        when(trip.getStatus()).thenReturn(TripStatus.PUBLISHED);
        when(trip.getDepartureDate())
                .thenReturn(LocalDateTime.now().minusHours(1));

        when(tripRepository.save(trip)).thenReturn(trip);
        when(tripMapper.toResponseDTO(trip)).thenReturn(response);

        TripResponseDTO result = tripService.complete(10L, 1L);

        assertThat(result).isEqualTo(response);

        verify(trip).setStatus(TripStatus.COMPLETED);
        verify(tripRepository).save(trip);
    }

    @Test
    void complete_shouldThrowException_whenTripDoesNotExist() {

        when(tripRepository.findById(10L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> tripService.complete(10L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void complete_shouldThrowException_whenDriverIsNotOwner() {

        Driver anotherDriver = mock(Driver.class);

        when(tripRepository.findById(10L))
                .thenReturn(Optional.of(trip));

        when(trip.getDriver()).thenReturn(anotherDriver);
        when(anotherDriver.getId()).thenReturn(99L);

        assertThatThrownBy(() -> tripService.complete(10L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("You are not the driver of this trip");
    }

    @Test
    void complete_shouldThrowException_whenTripCannotBeCompleted() {

        when(tripRepository.findById(10L))
                .thenReturn(Optional.of(trip));

        when(trip.getDriver()).thenReturn(driver);
        when(driver.getId()).thenReturn(1L);

        when(trip.getStatus()).thenReturn(TripStatus.CANCELLED);

        assertThatThrownBy(() -> tripService.complete(10L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("This trip cannot be completed");
    }

    @Test
    void complete_shouldThrowException_whenDepartureHasNotPassed() {

        when(tripRepository.findById(10L))
                .thenReturn(Optional.of(trip));

        when(trip.getDriver()).thenReturn(driver);
        when(driver.getId()).thenReturn(1L);

        when(trip.getStatus()).thenReturn(TripStatus.PUBLISHED);

        when(trip.getDepartureDate())
                .thenReturn(LocalDateTime.now().plusDays(1));

        assertThatThrownBy(() -> tripService.complete(10L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("You cannot complete a trip before departure");
    }

    // ==========================================
    // CANCEL
    // ==========================================

    @Test
    void cancel_shouldCancelTripAndConfirmedBookings() {

        Booking booking = mock(Booking.class);

        when(tripRepository.findById(10L))
                .thenReturn(Optional.of(trip));

        when(trip.getDriver()).thenReturn(driver);
        when(driver.getId()).thenReturn(1L);

        when(trip.getStatus()).thenReturn(TripStatus.PUBLISHED);

        when(bookingRepository.findByTripIdAndStatus(
                10L, BookingStatus.CONFIRMED))
                .thenReturn(List.of(booking));

        when(tripRepository.save(trip)).thenReturn(trip);
        when(tripMapper.toResponseDTO(trip)).thenReturn(response);

        TripResponseDTO result = tripService.cancel(10L, 1L);

        assertThat(result).isEqualTo(response);

        verify(booking).setStatus(BookingStatus.CANCELLED);
        verify(trip).setStatus(TripStatus.CANCELLED);
        verify(tripRepository).save(trip);
    }

    @Test
    void cancel_shouldCancelTrip_whenThereAreNoBookings() {

        when(tripRepository.findById(10L))
                .thenReturn(Optional.of(trip));

        when(trip.getDriver()).thenReturn(driver);
        when(driver.getId()).thenReturn(1L);

        when(trip.getStatus()).thenReturn(TripStatus.FULL);

        when(bookingRepository.findByTripIdAndStatus(
                10L, BookingStatus.CONFIRMED))
                .thenReturn(List.of());

        when(tripRepository.save(trip)).thenReturn(trip);
        when(tripMapper.toResponseDTO(trip)).thenReturn(response);

        tripService.cancel(10L, 1L);

        verify(trip).setStatus(TripStatus.CANCELLED);
    }

    @Test
    void cancel_shouldThrowException_whenTripDoesNotExist() {

        when(tripRepository.findById(10L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> tripService.cancel(10L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void cancel_shouldThrowException_whenDriverIsNotOwner() {

        Driver anotherDriver = mock(Driver.class);

        when(tripRepository.findById(10L))
                .thenReturn(Optional.of(trip));

        when(trip.getDriver()).thenReturn(anotherDriver);
        when(anotherDriver.getId()).thenReturn(99L);

        assertThatThrownBy(() -> tripService.cancel(10L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("You are not the driver of this trip");
    }

    @Test
    void cancel_shouldThrowException_whenTripCannotBeCancelled() {

        when(tripRepository.findById(10L))
                .thenReturn(Optional.of(trip));

        when(trip.getDriver()).thenReturn(driver);
        when(driver.getId()).thenReturn(1L);

        when(trip.getStatus()).thenReturn(TripStatus.COMPLETED);

        assertThatThrownBy(() -> tripService.cancel(10L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("This trip cannot be cancelled");

        verify(bookingRepository, never())
                .findByTripIdAndStatus(anyLong(), any());
    }

    // ==========================================
    // GET REMAINING SEATS
    // ==========================================

    @Test
    void getRemainingSeats_shouldReturnCorrectNumberOfSeats() {

        when(tripRepository.findById(10L))
                .thenReturn(Optional.of(trip));

        when(trip.getSeatsOffered()).thenReturn(4);

        when(bookingRepository.sumConfirmedSeatsByTripId(
                10L, BookingStatus.CONFIRMED))
                .thenReturn(2L);

        int result = tripService.getRemainingSeats(10L);

        assertThat(result).isEqualTo(2);

        verify(bookingRepository).sumConfirmedSeatsByTripId(
                10L, BookingStatus.CONFIRMED);
    }

    @Test
    void getRemainingSeats_shouldReturnAllSeats_whenNoSeatsAreBooked() {

        when(tripRepository.findById(10L))
                .thenReturn(Optional.of(trip));

        when(trip.getSeatsOffered()).thenReturn(4);

        when(bookingRepository.sumConfirmedSeatsByTripId(
                10L, BookingStatus.CONFIRMED))
                .thenReturn(0L);

        int result = tripService.getRemainingSeats(10L);

        assertThat(result).isEqualTo(4);
    }

    @Test
    void getRemainingSeats_shouldThrowException_whenTripDoesNotExist() {

        when(tripRepository.findById(10L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> tripService.getRemainingSeats(10L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Trip not found");
    }

    // ==========================================
    // HELPERS
    // ==========================================

    private void mockCreateDependencies() {

        when(driverRepository.findById(1L))
                .thenReturn(Optional.of(driver));

        when(vehicleRepository.findById(2L))
                .thenReturn(Optional.of(vehicle));

        when(vehicle.getDriver()).thenReturn(driver);
        when(driver.getId()).thenReturn(1L);
        when(vehicle.getCapacity()).thenReturn(4);

        doNothing().when(stoppingPointService)
                .validatePoints(requestPoints);
    }

    private void mockUpdateBeforeVehicleLookup() {

        Driver tripDriver = mock(Driver.class);

        when(tripRepository.findById(10L))
                .thenReturn(Optional.of(trip));

        when(trip.getStatus()).thenReturn(TripStatus.PUBLISHED);

        when(trip.getDriver()).thenReturn(tripDriver);
        when(tripDriver.getId()).thenReturn(1L);

        when(bookingRepository.existsByTripId(10L))
                .thenReturn(false);
    }

    private void mockUpdateDependencies(
            List<StoppingPoint> oldPoints,
            List<StoppingPoint> newPoints) {

        mockUpdateBeforeVehicleLookup();

        when(vehicleRepository.findById(2L))
                .thenReturn(Optional.of(vehicle));

        when(vehicle.getDriver()).thenReturn(driver);
        when(driver.getId()).thenReturn(1L);
        when(vehicle.getCapacity()).thenReturn(4);

        when(trip.getStoppingPoints()).thenReturn(oldPoints);

        doNothing().when(stoppingPointService)
                .validatePoints(requestPoints);
    }
}
