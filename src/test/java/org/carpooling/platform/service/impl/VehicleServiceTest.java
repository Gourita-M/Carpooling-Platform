
package org.carpooling.platform.service.impl;

import org.carpooling.platform.dto.request.VehicleRequestDTO;
import org.carpooling.platform.dto.response.VehicleResponseDTO;
import org.carpooling.platform.entity.Driver;
import org.carpooling.platform.entity.Vehicle;
import org.carpooling.platform.exception.BusinessException;
import org.carpooling.platform.exception.ResourceNotFoundException;
import org.carpooling.platform.mapper.VehicleMapper;
import org.carpooling.platform.repository.DriverRepository;
import org.carpooling.platform.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository repository;

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private VehicleMapper mapper;

    @InjectMocks
    private VehicleServiceImpl vehicleService;

    private Driver driver;
    private Vehicle vehicle;
    private VehicleRequestDTO request;
    private VehicleResponseDTO response;

    @BeforeEach
    void setUp() {
        driver = mock(Driver.class);
        vehicle = mock(Vehicle.class);
        request = mock(VehicleRequestDTO.class);
        response = mock(VehicleResponseDTO.class);
    }

    // ========================= CREATE =========================

    @Test
    void create_shouldCreateVehicleSuccessfully() {
        Long driverId = 1L;

        when(driverRepository.findById(driverId)).thenReturn(Optional.of(driver));
        when(repository.existsByRegistrationNumber(request.registrationNumber())).thenReturn(false);
        when(mapper.toEntity(request)).thenReturn(vehicle);
        when(repository.save(vehicle)).thenReturn(vehicle);
        when(mapper.toResponseDTO(vehicle)).thenReturn(response);

        VehicleResponseDTO result = vehicleService.create(request, driverId);

        assertThat(result).isEqualTo(response);

        verify(vehicle).setDriver(driver);
        verify(repository).save(vehicle);
        verify(mapper).toResponseDTO(vehicle);
    }

    @Test
    void create_shouldThrowExceptionWhenDriverDoesNotExist() {
        Long driverId = 1L;

        when(driverRepository.findById(driverId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.create(request, driverId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Driver is Not Found");

        verifyNoInteractions(mapper);
        verify(repository, never()).save(any());
    }

    @Test
    void create_shouldThrowExceptionWhenRegistrationNumberAlreadyExists() {
        Long driverId = 1L;

        when(driverRepository.findById(driverId)).thenReturn(Optional.of(driver));
        when(repository.existsByRegistrationNumber(request.registrationNumber())).thenReturn(true);

        assertThatThrownBy(() -> vehicleService.create(request, driverId))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Registration Number Already Exists");

        verify(mapper, never()).toEntity(any());
        verify(repository, never()).save(any());
    }

    // ========================= FIND BY ID =========================

    @Test
    void findById_shouldReturnVehicleWhenItExists() {
        Long vehicleId = 1L;

        when(repository.findById(vehicleId)).thenReturn(Optional.of(vehicle));
        when(mapper.toResponseDTO(vehicle)).thenReturn(response);

        VehicleResponseDTO result = vehicleService.findById(vehicleId);

        assertThat(result).isEqualTo(response);

        verify(repository).findById(vehicleId);
        verify(mapper).toResponseDTO(vehicle);
    }

    @Test
    void findById_shouldThrowExceptionWhenVehicleDoesNotExist() {
        Long vehicleId = 1L;

        when(repository.findById(vehicleId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.findById(vehicleId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Vehicle is Not Found");

        verify(mapper, never()).toResponseDTO(any());
    }

    // ========================= FIND BY DRIVER ID =========================

    @Test
    void findByDriverId_shouldReturnVehiclesForDriver() {
        Long driverId = 1L;

        Vehicle secondVehicle = mock(Vehicle.class);
        VehicleResponseDTO secondResponse = mock(VehicleResponseDTO.class);

        when(repository.findAllByDriverId(driverId))
                .thenReturn(List.of(vehicle, secondVehicle));

        when(mapper.toResponseDTO(vehicle)).thenReturn(response);
        when(mapper.toResponseDTO(secondVehicle)).thenReturn(secondResponse);

        List<VehicleResponseDTO> result = vehicleService.findByDriverId(driverId);

        assertThat(result)
                .hasSize(2)
                .containsExactly(response, secondResponse);

        verify(repository).findAllByDriverId(driverId);
        verify(mapper, times(2)).toResponseDTO(any(Vehicle.class));
    }

    @Test
    void findByDriverId_shouldReturnEmptyListWhenDriverHasNoVehicles() {
        Long driverId = 1L;

        when(repository.findAllByDriverId(driverId)).thenReturn(List.of());

        List<VehicleResponseDTO> result = vehicleService.findByDriverId(driverId);

        assertThat(result).isEmpty();

        verify(mapper, never()).toResponseDTO(any());
    }

    // ========================= UPDATE =========================

    @Test
    void update_shouldUpdateVehicleSuccessfully() {
        Long vehicleId = 1L;

        when(repository.findById(vehicleId)).thenReturn(Optional.of(vehicle));
        when(repository.save(vehicle)).thenReturn(vehicle);
        when(mapper.toResponseDTO(vehicle)).thenReturn(response);

        VehicleResponseDTO result = vehicleService.update(vehicleId, request);

        assertThat(result).isEqualTo(response);

        verify(vehicle).setMake(request.make());
        verify(vehicle).setModel(request.model());
        verify(vehicle).setRegistrationNumber(request.registrationNumber());
        verify(vehicle).setColor(request.color());
        verify(vehicle).setCapacity(request.capacity());

        verify(repository).save(vehicle);
        verify(mapper).toResponseDTO(vehicle);
    }

    @Test
    void update_shouldThrowExceptionWhenVehicleDoesNotExist() {
        Long vehicleId = 1L;

        when(repository.findById(vehicleId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.update(vehicleId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Vehicle Is Not Found");

        verify(repository, never()).save(any());
        verifyNoInteractions(mapper);
    }

    // ========================= DELETE =========================

    @Test
    void delete_shouldDeleteVehicleSuccessfully() {
        Long vehicleId = 1L;

        when(repository.findById(vehicleId)).thenReturn(Optional.of(vehicle));

        vehicleService.delete(vehicleId);

        verify(repository).findById(vehicleId);
        verify(repository).delete(vehicle);
    }

    @Test
    void delete_shouldThrowExceptionWhenVehicleDoesNotExist() {
        Long vehicleId = 1L;

        when(repository.findById(vehicleId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.delete(vehicleId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Vehicle Is Not Found");

        verify(repository, never()).delete(any());
    }
}
