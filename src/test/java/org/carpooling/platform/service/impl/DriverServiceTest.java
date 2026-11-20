
package org.carpooling.platform.service.impl;

import org.carpooling.platform.dto.request.DriverRequestDTO;
import org.carpooling.platform.dto.response.DriverResponseDTO;
import org.carpooling.platform.entity.Driver;
import org.carpooling.platform.mapper.DriverMapper;
import org.carpooling.platform.repository.DriverRepository;

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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DriverServiceTest {

    @Mock
    private DriverRepository repository;

    @Mock
    private DriverMapper mapper;

    @InjectMocks
    private DriverServiceImpl driverService;

    private Driver driver;
    private Driver updatedDriver;
    private DriverRequestDTO request;
    private DriverResponseDTO response;

    @BeforeEach
    void setUp() {
        driver = mock(Driver.class);
        updatedDriver = mock(Driver.class);
        request = mock(DriverRequestDTO.class);
        response = mock(DriverResponseDTO.class);
    }

    @Test
    void create_shouldCreateDriverSuccessfully() {

        when(mapper.toEntity(request)).thenReturn(driver);
        when(repository.save(driver)).thenReturn(driver);
        when(mapper.toResponseDTO(driver)).thenReturn(response);

        DriverResponseDTO result = driverService.create(request);

        assertThat(result).isEqualTo(response);

        verify(mapper).toEntity(request);
        verify(repository).save(driver);
        verify(mapper).toResponseDTO(driver);
    }

    @Test
    void create_shouldCallMapperAndRepository() {

        when(mapper.toEntity(request)).thenReturn(driver);
        when(repository.save(driver)).thenReturn(driver);
        when(mapper.toResponseDTO(driver)).thenReturn(response);

        driverService.create(request);

        verify(repository, times(1)).save(driver);
    }

    @Test
    void findById_shouldReturnDriverSuccessfully() {

        when(repository.findById(1L)).thenReturn(Optional.of(driver));
        when(mapper.toResponseDTO(driver)).thenReturn(response);

        DriverResponseDTO result = driverService.findById(1L);

        assertThat(result).isEqualTo(response);

        verify(repository).findById(1L);
        verify(mapper).toResponseDTO(driver);
    }

    @Test
    void findById_shouldThrowException_whenDriverDoesNotExist() {

        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> driverService.findById(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Driver is not Found");

        verify(mapper, never()).toResponseDTO(any());
    }

    @Test
    void findAll_shouldReturnDriversSuccessfully() {

        Pageable pageable = PageRequest.of(0, 10);

        Driver secondDriver = mock(Driver.class);
        DriverResponseDTO secondResponse = mock(DriverResponseDTO.class);

        Page<Driver> driverPage =
                new PageImpl<>(List.of(driver, secondDriver));

        when(repository.findAll(pageable)).thenReturn(driverPage);
        when(mapper.toResponseDTO(driver)).thenReturn(response);
        when(mapper.toResponseDTO(secondDriver)).thenReturn(secondResponse);

        Page<DriverResponseDTO> result =
                driverService.findAll(pageable);

        assertThat(result.getContent())
                .containsExactly(response, secondResponse);

        assertThat(result.getTotalElements()).isEqualTo(2);

        verify(repository).findAll(pageable);
        verify(mapper, times(2)).toResponseDTO(any(Driver.class));
    }

    @Test
    void findAll_shouldReturnEmptyPage_whenNoDriversExist() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<Driver> emptyPage = Page.empty(pageable);

        when(repository.findAll(pageable)).thenReturn(emptyPage);

        Page<DriverResponseDTO> result =
                driverService.findAll(pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();

        verify(repository).findAll(pageable);
        verifyNoInteractions(mapper);
    }

    @Test
    void search_shouldReturnMatchingDrivers() {

        String keyword = "Mouad";
        Pageable pageable = PageRequest.of(0, 10);

        Page<Driver> driverPage =
                new PageImpl<>(List.of(driver));

        when(repository
                .findByFirstNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                        keyword, keyword, pageable))
                .thenReturn(driverPage);

        when(mapper.toResponseDTO(driver)).thenReturn(response);

        Page<DriverResponseDTO> result =
                driverService.search(keyword, pageable);

        assertThat(result.getContent()).containsExactly(response);

        verify(repository)
                .findByFirstNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                        keyword, keyword, pageable);

        verify(mapper).toResponseDTO(driver);
    }

    @Test
    void search_shouldReturnEmptyPage_whenNoDriversMatch() {

        String keyword = "Unknown";
        Pageable pageable = PageRequest.of(0, 10);

        when(repository
                .findByFirstNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                        keyword, keyword, pageable))
                .thenReturn(Page.empty(pageable));

        Page<DriverResponseDTO> result =
                driverService.search(keyword, pageable);

        assertThat(result.getContent()).isEmpty();

        verify(repository)
                .findByFirstNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                        keyword, keyword, pageable);

        verifyNoInteractions(mapper);
    }

    @Test
    void update_shouldUpdateDriverSuccessfully() {

        when(repository.findById(1L)).thenReturn(Optional.of(driver));
        when(mapper.toEntity(request)).thenReturn(updatedDriver);
        when(repository.save(updatedDriver)).thenReturn(updatedDriver);
        when(mapper.toResponseDTO(updatedDriver)).thenReturn(response);

        DriverResponseDTO result =
                driverService.update(1L, request);

        assertThat(result).isEqualTo(response);

        verify(repository).findById(1L);
        verify(mapper).toEntity(request);
        verify(updatedDriver).setId(1L);
        verify(repository).save(updatedDriver);
        verify(mapper).toResponseDTO(updatedDriver);
    }

    @Test
    void update_shouldThrowException_whenDriverDoesNotExist() {

        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> driverService.update(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("There is No Driver With this ID");

        verify(mapper, never()).toEntity(any());
        verify(repository, never()).save(any());
    }

    @Test
    void delete_shouldDeleteDriverSuccessfully() {

        when(repository.findById(1L)).thenReturn(Optional.of(driver));

        driverService.delete(1L);

        verify(repository).findById(1L);
        verify(repository).delete(driver);
    }

    @Test
    void delete_shouldThrowException_whenDriverDoesNotExist() {

        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> driverService.delete(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("There is No Driver With this ID");

        verify(repository, never()).delete(any());
    }
}
