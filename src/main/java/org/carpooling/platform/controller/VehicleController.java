package org.carpooling.platform.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.carpooling.platform.dto.request.VehicleRequestDTO;
import org.carpooling.platform.dto.response.VehicleResponseDTO;
import org.carpooling.platform.service.VehicleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @PostMapping
    public ResponseEntity<VehicleResponseDTO> create(
            @Valid @RequestBody VehicleRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(vehicleService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponseDTO> findById(
            @PathVariable Long id) {
        return ResponseEntity.ok(vehicleService.findById(id));
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<VehicleResponseDTO>> findByDriverId(
            @PathVariable Long driverId) {
        return ResponseEntity.ok(vehicleService.findByDriverId(driverId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehicleResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody VehicleRequestDTO request) {
        return ResponseEntity.ok(vehicleService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        vehicleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
