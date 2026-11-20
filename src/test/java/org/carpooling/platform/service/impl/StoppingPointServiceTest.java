
package org.carpooling.platform.service.impl;

import org.carpooling.platform.dto.request.StoppingPointRequestDTO;
import org.carpooling.platform.dto.response.StoppingPointResponseDTO;
import org.carpooling.platform.entity.StoppingPoint;
import org.carpooling.platform.exception.BusinessException;
import org.carpooling.platform.exception.ResourceNotFoundException;
import org.carpooling.platform.mapper.StoppingPointMapper;
import org.carpooling.platform.repository.StoppingPointRepository;
import org.carpooling.platform.repository.TripRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoppingPointServiceTest {

    @Mock
    private StoppingPointRepository stoppingPointRepository;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private StoppingPointMapper stoppingPointMapper;

    @InjectMocks
    private StoppingPointServiceImpl stoppingPointService;

    private StoppingPoint stoppingPoint;
    private StoppingPointResponseDTO response;

    @BeforeEach
    void setUp() {
        stoppingPoint = mock(StoppingPoint.class);
        response = mock(StoppingPointResponseDTO.class);
    }

    // ==========================================
    // FIND BY ID
    // ==========================================

    @Test
    void findById_shouldReturnStoppingPointSuccessfully() {

        when(stoppingPointRepository.findById(1L))
                .thenReturn(Optional.of(stoppingPoint));

        when(stoppingPointMapper.toResponseDTO(stoppingPoint))
                .thenReturn(response);

        StoppingPointResponseDTO result =
                stoppingPointService.findById(1L);

        assertThat(result).isEqualTo(response);

        verify(stoppingPointRepository).findById(1L);
        verify(stoppingPointMapper).toResponseDTO(stoppingPoint);
    }

    @Test
    void findById_shouldThrowException_whenStoppingPointDoesNotExist() {

        when(stoppingPointRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> stoppingPointService.findById(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Stopping point not found");

        verifyNoInteractions(stoppingPointMapper);
    }

    // ==========================================
    // FIND BY TRIP ID
    // ==========================================

    @Test
    void findByTripId_shouldReturnStoppingPointsInOrder() {

        StoppingPoint secondPoint = mock(StoppingPoint.class);

        when(tripRepository.existsById(10L)).thenReturn(true);

        when(stoppingPointRepository
                .findByTripIdOrderByPointOrderAsc(10L))
                .thenReturn(List.of(stoppingPoint, secondPoint));

        when(stoppingPointMapper.toResponseDTO(stoppingPoint))
                .thenReturn(response);

        StoppingPointResponseDTO secondResponse =
                mock(StoppingPointResponseDTO.class);

        when(stoppingPointMapper.toResponseDTO(secondPoint))
                .thenReturn(secondResponse);

        List<StoppingPointResponseDTO> result =
                stoppingPointService.findByTripId(10L);

        assertThat(result)
                .containsExactly(response, secondResponse);

        verify(tripRepository).existsById(10L);

        verify(stoppingPointRepository)
                .findByTripIdOrderByPointOrderAsc(10L);

        verify(stoppingPointMapper, times(2))
                .toResponseDTO(any(StoppingPoint.class));
    }

    @Test
    void findByTripId_shouldReturnEmptyList_whenTripHasNoStoppingPoints() {

        when(tripRepository.existsById(10L)).thenReturn(true);

        when(stoppingPointRepository
                .findByTripIdOrderByPointOrderAsc(10L))
                .thenReturn(List.of());

        List<StoppingPointResponseDTO> result =
                stoppingPointService.findByTripId(10L);

        assertThat(result).isEmpty();

        verify(stoppingPointRepository)
                .findByTripIdOrderByPointOrderAsc(10L);

        verifyNoInteractions(stoppingPointMapper);
    }

    @Test
    void findByTripId_shouldThrowException_whenTripDoesNotExist() {

        when(tripRepository.existsById(10L)).thenReturn(false);

        assertThatThrownBy(() ->
                stoppingPointService.findByTripId(10L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Trip not found");

        verify(stoppingPointRepository, never())
                .findByTripIdOrderByPointOrderAsc(anyLong());

        verifyNoInteractions(stoppingPointMapper);
    }

    // ==========================================
    // VALIDATE POINTS
    // ==========================================

    @Test
    void validatePoints_shouldAcceptValidPoints() {

        LocalDateTime departure = LocalDateTime.now().plusDays(2);

        StoppingPointRequestDTO first =
                point("Safi", 0, departure, "0.00");

        StoppingPointRequestDTO second =
                point("Casablanca", 1, departure.plusHours(3), "50.00");

        StoppingPointRequestDTO third =
                point("Rabat", 2, departure.plusHours(4), "70.00");

        List<StoppingPointRequestDTO> points =
                List.of(first, second, third);

        // No exception should be thrown.
        stoppingPointService.validatePoints(points);
    }

    @Test
    void validatePoints_shouldThrowException_whenPointsAreNull() {

        assertThatThrownBy(() ->
                stoppingPointService.validatePoints(null))
                .isInstanceOf(BusinessException.class)
                .hasMessage("A trip must have at least two stopping points");
    }

    @Test
    void validatePoints_shouldThrowException_whenLessThanTwoPoints() {

        List<StoppingPointRequestDTO> points =
                List.of(point(
                        "Safi",
                        0,
                        LocalDateTime.now().plusDays(2),
                        "0.00"
                ));

        assertThatThrownBy(() ->
                stoppingPointService.validatePoints(points))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void validatePoints_shouldThrowException_whenCityIsNull() {

        StoppingPointRequestDTO first =
                point(null, 0, LocalDateTime.now().plusDays(2), "0.00");

        StoppingPointRequestDTO second =
                point("Casablanca", 1,
                        LocalDateTime.now().plusDays(2).plusHours(2),
                        "50.00");

        assertThatThrownBy(() ->
                stoppingPointService.validatePoints(List.of(first, second)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("City cannot be empty");
    }

    @Test
    void validatePoints_shouldThrowException_whenCityIsBlank() {

        StoppingPointRequestDTO first =
                point("   ", 0, LocalDateTime.now().plusDays(2), "0.00");

        StoppingPointRequestDTO second =
                point("Casablanca", 1,
                        LocalDateTime.now().plusDays(2).plusHours(2),
                        "50.00");

        assertThatThrownBy(() ->
                stoppingPointService.validatePoints(List.of(first, second)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("City cannot be empty");
    }

    @Test
    void validatePoints_shouldThrowException_whenCityIsRepeated() {

        LocalDateTime departure = LocalDateTime.now().plusDays(2);

        StoppingPointRequestDTO first =
                point("Safi", 0, departure, "0.00");

        StoppingPointRequestDTO second =
                point("sAFI", 1, departure.plusHours(2), "50.00");

        assertThatThrownBy(() ->
                stoppingPointService.validatePoints(List.of(first, second)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("A city cannot appear twice in the same trip");
    }

    @Test
    void validatePoints_shouldThrowException_whenPointOrderIsNotConsecutive() {

        LocalDateTime departure = LocalDateTime.now().plusDays(2);

        StoppingPointRequestDTO first =
                point("Safi", 0, departure, "0.00");

        StoppingPointRequestDTO second =
                point("Casablanca", 2, departure.plusHours(2), "50.00");

        assertThatThrownBy(() ->
                stoppingPointService.validatePoints(List.of(first, second)))
                .isInstanceOf(BusinessException.class)
                .hasMessage(
                        "Stopping point order must start at 0 and be consecutive");
    }

    @Test
    void validatePoints_shouldThrowException_whenPassingTimeIsNull() {

        LocalDateTime departure = LocalDateTime.now().plusDays(2);

        StoppingPointRequestDTO first =
                point("Safi", 0, departure, "0.00");

        StoppingPointRequestDTO second =
                point("Casablanca", 1, null, "50.00");

        assertThatThrownBy(() ->
                stoppingPointService.validatePoints(List.of(first, second)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Passing time and price are required");
    }

    @Test
    void validatePoints_shouldThrowException_whenPriceIsNull() {

        LocalDateTime departure = LocalDateTime.now().plusDays(2);

        StoppingPointRequestDTO first =
                point("Safi", 0, departure, null);

        StoppingPointRequestDTO second =
                point("Casablanca", 1, departure.plusHours(2), "50.00");

        assertThatThrownBy(() ->
                stoppingPointService.validatePoints(List.of(first, second)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Passing time and price are required");
    }

    @Test
    void validatePoints_shouldThrowException_whenPriceIsNegative() {

        LocalDateTime departure = LocalDateTime.now().plusDays(2);

        StoppingPointRequestDTO first =
                point("Safi", 0, departure, "0.00");

        StoppingPointRequestDTO second =
                point("Casablanca", 1, departure.plusHours(2), "-10.00");

        assertThatThrownBy(() ->
                stoppingPointService.validatePoints(List.of(first, second)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Price cannot be negative");
    }

    @Test
    void validatePoints_shouldThrowException_whenDepartureIsInThePast() {

        LocalDateTime departure = LocalDateTime.now().minusDays(1);

        StoppingPointRequestDTO first =
                point("Safi", 0, departure, "0.00");

        StoppingPointRequestDTO second =
                point("Casablanca", 1, departure.plusHours(2), "50.00");

        assertThatThrownBy(() ->
                stoppingPointService.validatePoints(List.of(first, second)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Departure must be in the future");
    }

    @Test
    void validatePoints_shouldThrowException_whenDeparturePriceIsNotZero() {

        LocalDateTime departure = LocalDateTime.now().plusDays(2);

        StoppingPointRequestDTO first =
                point("Safi", 0, departure, "10.00");

        StoppingPointRequestDTO second =
                point("Casablanca", 1, departure.plusHours(2), "50.00");

        assertThatThrownBy(() ->
                stoppingPointService.validatePoints(List.of(first, second)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Departure point price must be zero");
    }

    @Test
    void validatePoints_shouldThrowException_whenPassingTimesAreNotIncreasing() {

        LocalDateTime departure = LocalDateTime.now().plusDays(2);

        StoppingPointRequestDTO first =
                point("Safi", 0, departure, "0.00");

        StoppingPointRequestDTO second =
                point("Casablanca", 1, departure, "50.00");

        assertThatThrownBy(() ->
                stoppingPointService.validatePoints(List.of(first, second)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Passing times must be strictly increasing");
    }

    @Test
    void validatePoints_shouldThrowException_whenPricesAreNotIncreasing() {

        LocalDateTime departure = LocalDateTime.now().plusDays(2);

        StoppingPointRequestDTO first =
                point("Safi", 0, departure, "0.00");

        StoppingPointRequestDTO second =
                point("Casablanca", 1, departure.plusHours(2), "0.00");

        assertThatThrownBy(() ->
                stoppingPointService.validatePoints(List.of(first, second)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Prices must strictly increase");
    }

    // ==========================================
    // HELPER
    // ==========================================

    private StoppingPointRequestDTO point(
            String city,
            int order,
            LocalDateTime passingTime,
            String price) {

        StoppingPointRequestDTO dto =
                mock(StoppingPointRequestDTO.class);

        when(dto.city()).thenReturn(city);
        when(dto.pointOrder()).thenReturn(order);
        when(dto.passingTime()).thenReturn(passingTime);

        when(dto.priceFromDeparture()).thenReturn(
                price == null ? null : new BigDecimal(price)
        );

        return dto;
    }
}
