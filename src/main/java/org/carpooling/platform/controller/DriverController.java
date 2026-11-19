package org.carpooling.platform.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.carpooling.platform.dto.request.DriverRequestDTO;
import org.carpooling.platform.dto.response.DriverResponseDTO;
import org.carpooling.platform.service.DriverService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;

    //PostMan Done
    @PostMapping
    public ResponseEntity<DriverResponseDTO> create(
            @Valid @RequestBody DriverRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(driverService.create(request));
    }

    //PostMan Done
    @GetMapping("/{id}")
    public ResponseEntity<DriverResponseDTO> findById(
            @PathVariable Long id) {
        return ResponseEntity.ok(driverService.findById(id));
    }

    //PostMan Done
    @GetMapping
    public ResponseEntity<Page<DriverResponseDTO>> findAll(
            Pageable pageable) {
        return ResponseEntity.ok(driverService.findAll(pageable));
    }

    //PostMan Done
    @GetMapping("/search")
    public ResponseEntity<Page<DriverResponseDTO>> search(
            @RequestParam String keyword,
            Pageable pageable) {
        return ResponseEntity.ok(driverService.search(keyword, pageable));
    }

    //PostMan Done
    @PutMapping("/{id}")
    public ResponseEntity<DriverResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody DriverRequestDTO request) {
        return ResponseEntity.ok(driverService.update(id, request));
    }

    //PostMan Done
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        driverService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
