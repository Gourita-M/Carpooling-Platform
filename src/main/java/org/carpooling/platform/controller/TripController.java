package org.carpooling.platform.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.carpooling.platform.dto.request.TripRequestDTO;
import org.carpooling.platform.dto.response.BookingResponseDTO;
import org.carpooling.platform.dto.response.TripResponseDTO;
import org.carpooling.platform.service.BookingService;
import org.carpooling.platform.service.TripService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<TripResponseDTO> create(
            @Valid @RequestBody TripRequestDTO request,
            @RequestParam Long driverId,
            @RequestParam Long vehicleId) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tripService.create(request, driverId, vehicleId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TripResponseDTO> findById(
            @PathVariable Long id) {
        return ResponseEntity.ok(tripService.findById(id));
    }

    @GetMapping
    public ResponseEntity<Page<TripResponseDTO>> findAll(
            Pageable pageable) {
        return ResponseEntity.ok(tripService.findAll(pageable));
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<Page<TripResponseDTO>> findByDriverId(
            @PathVariable Long driverId,
            Pageable pageable) {
        return ResponseEntity.ok(
                tripService.findByDriverId(driverId, pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<TripResponseDTO>> search(
            @RequestParam String departureCity,
            @RequestParam String destinationCity,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            @RequestParam(required = false) Integer numberOfSeats,
            @RequestParam(required = false) BigDecimal maxPrice,
            Pageable pageable) {

        return ResponseEntity.ok(
                tripService.search(
                        departureCity,
                        destinationCity,
                        date,
                        numberOfSeats,
                        maxPrice,
                        pageable
                ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TripResponseDTO> update(
            @PathVariable Long id,
            @RequestParam Long driverId,
            @Valid @RequestBody TripRequestDTO request) {

        return ResponseEntity.ok(
                tripService.update(id, request, driverId));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<TripResponseDTO> complete(
            @PathVariable Long id,
            @RequestParam Long driverId) {
        return ResponseEntity.ok(tripService.complete(id, driverId));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(
            @PathVariable Long id,
            @RequestParam Long driverId) {
        tripService.cancel(id, driverId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/remaining-seats")
    public ResponseEntity<Integer> getRemainingSeats(
            @PathVariable Long id) {
        return ResponseEntity.ok(tripService.getRemainingSeats(id));
    }

    @GetMapping("/{id}/bookings")
    public ResponseEntity<Page<BookingResponseDTO>> getBookings(
            @PathVariable Long id,
            Pageable pageable) {
        return ResponseEntity.ok(
                bookingService.findByTripId(id, pageable));
    }
}
