
package org.carpooling.platform.service.impl;

import org.carpooling.platform.dto.request.PassengerRequestDTO;
import org.carpooling.platform.dto.response.PassengerResponseDTO;
import org.carpooling.platform.entity.Passenger;
import org.carpooling.platform.mapper.PassengerMapper;
import org.carpooling.platform.repository.PassengerRepository;

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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PassengerServiceTest {

    @Mock
    private PassengerRepository repository;

    @Mock
    private PassengerMapper mapper;

    @InjectMocks
    private PassengerServiceImpl passengerService;

    private Passenger passenger;
    private Passenger updatedPassenger;
    private PassengerRequestDTO request;
    private PassengerResponseDTO response;

    @BeforeEach
    void setUp() {
        passenger = mock(Passenger.class);
        updatedPassenger = mock(Passenger.class);
        request = mock(PassengerRequestDTO.class);
        response = mock(PassengerResponseDTO.class);
    }

    @Test
    void create_shouldCreatePassengerSuccessfully() {

        when(mapper.toEntity(request)).thenReturn(passenger);
        when(repository.save(passenger)).thenReturn(passenger);
        when(mapper.toResponseDTO(passenger)).thenReturn(response);

        PassengerResponseDTO result = passengerService.create(request);

        assertThat(result).isEqualTo(response);

        verify(mapper).toEntity(request);
        verify(repository).save(passenger);
        verify(mapper).toResponseDTO(passenger);
    }

    @Test
    void findById_shouldReturnPassengerSuccessfully() {

        when(repository.findById(1L))
                .thenReturn(Optional.of(passenger));
        when(mapper.toResponseDTO(passenger))
                .thenReturn(response);

        PassengerResponseDTO result = passengerService.findById(1L);

        assertThat(result).isEqualTo(response);

        verify(repository).findById(1L);
        verify(mapper).toResponseDTO(passenger);
    }

    @Test
    void findById_shouldThrowException_whenPassengerDoesNotExist() {

        when(repository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> passengerService.findById(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Passenger is not Found");

        verify(mapper, never()).toResponseDTO(any());
    }

    @Test
    void findAll_shouldReturnPassengersSuccessfully() {

        Pageable pageable = PageRequest.of(0, 10);

        Passenger secondPassenger = mock(Passenger.class);
        PassengerResponseDTO secondResponse = mock(PassengerResponseDTO.class);

        Page<Passenger> passengerPage =
                new PageImpl<>(List.of(passenger, secondPassenger));

        when(repository.findAll(pageable)).thenReturn(passengerPage);
        when(mapper.toResponseDTO(passenger)).thenReturn(response);
        when(mapper.toResponseDTO(secondPassenger)).thenReturn(secondResponse);

        Page<PassengerResponseDTO> result =
                passengerService.findAll(pageable);

        assertThat(result.getContent())
                .containsExactly(response, secondResponse);

        assertThat(result.getTotalElements()).isEqualTo(2);

        verify(repository).findAll(pageable);
        verify(mapper, times(2)).toResponseDTO(any(Passenger.class));
    }

    @Test
    void findAll_shouldReturnEmptyPage_whenNoPassengersExist() {

        Pageable pageable = PageRequest.of(0, 10);

        when(repository.findAll(pageable))
                .thenReturn(Page.empty(pageable));

        Page<PassengerResponseDTO> result =
                passengerService.findAll(pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();

        verify(repository).findAll(pageable);
        verifyNoInteractions(mapper);
    }

    @Test
    void search_shouldReturnMatchingPassengers() {

        String email = "test@example.com";
        Pageable pageable = PageRequest.of(0, 10);

        Page<Passenger> passengerPage =
                new PageImpl<>(List.of(passenger));

        when(repository.findByEmailContainingIgnoreCase(email, pageable))
                .thenReturn(passengerPage);

        when(mapper.toResponseDTO(passenger))
                .thenReturn(response);

        Page<PassengerResponseDTO> result =
                passengerService.search(email, pageable);

        assertThat(result.getContent()).containsExactly(response);

        verify(repository)
                .findByEmailContainingIgnoreCase(email, pageable);

        verify(mapper).toResponseDTO(passenger);
    }

    @Test
    void search_shouldReturnEmptyPage_whenNoPassengersMatch() {

        String email = "unknown@example.com";
        Pageable pageable = PageRequest.of(0, 10);

        when(repository.findByEmailContainingIgnoreCase(email, pageable))
                .thenReturn(Page.empty(pageable));

        Page<PassengerResponseDTO> result =
                passengerService.search(email, pageable);

        assertThat(result.getContent()).isEmpty();

        verify(repository)
                .findByEmailContainingIgnoreCase(email, pageable);

        verifyNoInteractions(mapper);
    }

    @Test
    void update_shouldUpdatePassengerSuccessfully() {

        when(repository.findById(1L))
                .thenReturn(Optional.of(passenger));

        when(mapper.toEntity(request))
                .thenReturn(updatedPassenger);

        when(repository.save(updatedPassenger))
                .thenReturn(updatedPassenger);

        when(mapper.toResponseDTO(updatedPassenger))
                .thenReturn(response);

        PassengerResponseDTO result =
                passengerService.update(1L, request);

        assertThat(result).isEqualTo(response);

        verify(repository).findById(1L);
        verify(mapper).toEntity(request);
        verify(updatedPassenger).setId(1L);
        verify(repository).save(updatedPassenger);
        verify(mapper).toResponseDTO(updatedPassenger);
    }

    @Test
    void update_shouldThrowException_whenPassengerDoesNotExist() {

        when(repository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> passengerService.update(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Passenger Is Not Found");

        verify(mapper, never()).toEntity(any());
        verify(repository, never()).save(any());
    }

    @Test
    void delete_shouldDeletePassengerSuccessfully() {

        when(repository.findById(1L))
                .thenReturn(Optional.of(passenger));

        passengerService.delete(1L);

        verify(repository).findById(1L);
        verify(repository).delete(passenger);
    }

    @Test
    void delete_shouldThrowException_whenPassengerDoesNotExist() {

        when(repository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> passengerService.delete(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Passenger Is Not Found");

        verify(repository, never()).delete(any());
    }
}
