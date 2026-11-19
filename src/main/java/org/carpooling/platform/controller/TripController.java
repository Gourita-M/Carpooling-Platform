
package org.carpooling.platform.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.carpooling.platform.dto.request.TripRequestDTO;
import org.carpooling.platform.dto.response.TripResponseDTO;
import org.carpooling.platform.entity.Trip;
import org.carpooling.platform.repository.TripRepository;
import org.carpooling.platform.service.TripService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;
    //PostMan Done
    @PostMapping
    public ResponseEntity<TripResponseDTO> create(
            @Valid @RequestBody TripRequestDTO request) {

        TripResponseDTO response = tripService.create(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    //PostMan Done
    @GetMapping("/{id}")
    public ResponseEntity<TripResponseDTO> findById(
            @PathVariable Long id) {

        return ResponseEntity.ok(tripService.findById(id));
    }
    //PostMan Done
    @GetMapping
    public ResponseEntity<Page<TripResponseDTO>> findAll(
            @PageableDefault(size = 10, sort = "departureDate")
            Pageable pageable) {

        return ResponseEntity.ok(tripService.findAll(pageable));
    }
    //PostMan Done
    @GetMapping("/driver/{driverId}")
    public ResponseEntity<Page<TripResponseDTO>> findByDriverId(
            @PathVariable Long driverId,
            @PageableDefault(size = 10, sort = "departureDate")
            Pageable pageable) {

        return ResponseEntity.ok(
                tripService.findByDriverId(driverId, pageable));
    }
    //PostMan Done
    @GetMapping("/search")
    public ResponseEntity<Page<TripResponseDTO>> search(

            @RequestParam String departureCity,

            @RequestParam String destinationCity,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,

            @RequestParam(required = false)
            Integer numberOfSeats,

            @RequestParam(required = false)
            BigDecimal maxPrice,

            @PageableDefault(size = 10, sort = "departureDate")
            Pageable pageable) {

        return ResponseEntity.ok(
                tripService.search(
                        departureCity,
                        destinationCity,
                        date,
                        numberOfSeats,
                        maxPrice,
                        pageable
                )
        );
    }
    //PostMan Done
    @PutMapping("/{tripId}")
    public ResponseEntity<TripResponseDTO> update(
            @PathVariable Long tripId,
            @Valid @RequestBody TripRequestDTO request) {

        return ResponseEntity.ok(
                tripService.update(tripId, request));
    }
    //PostMan Done
    @PatchMapping("/{id}/complete")
    public ResponseEntity<TripResponseDTO> complete(
            @PathVariable Long id,
            @RequestParam Long driverId) {

        return ResponseEntity.ok(
                tripService.complete(id, driverId));
    }
    //PostMan Done
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<TripResponseDTO> cancel(
            @PathVariable Long id,
            @RequestParam Long driverId) {

        return ResponseEntity.ok(
                tripService.cancel(id, driverId));
    }
    //PostMan Done
    @GetMapping("/{id}/remaining-seats")
    public ResponseEntity<Integer> getRemainingSeats(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                tripService.getRemainingSeats(id));
    }

}
