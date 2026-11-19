package org.carpooling.platform.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.carpooling.platform.dto.request.PassengerRequestDTO;
import org.carpooling.platform.dto.response.BookingResponseDTO;
import org.carpooling.platform.dto.response.PassengerResponseDTO;
import org.carpooling.platform.service.BookingService;
import org.carpooling.platform.service.PassengerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/passengers")
@RequiredArgsConstructor
public class PassengerController {

    private final PassengerService passengerService;
    private final BookingService bookingService;

    //PostMan Done
    @PostMapping
    public ResponseEntity<PassengerResponseDTO> create(
            @Valid @RequestBody PassengerRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(passengerService.create(request));
    }
    //PostMan Done
    @GetMapping("/{id}")
    public ResponseEntity<PassengerResponseDTO> findById(
            @PathVariable Long id) {
        return ResponseEntity.ok(passengerService.findById(id));
    }
    //PostMan Done
    @GetMapping
    public ResponseEntity<Page<PassengerResponseDTO>> findAll(
            Pageable pageable) {
        return ResponseEntity.ok(passengerService.findAll(pageable));
    }
    //PostMan Done
    @GetMapping("/search")
    public ResponseEntity<Page<PassengerResponseDTO>> search(
            @RequestParam String email,
            Pageable pageable) {
        return ResponseEntity.ok(passengerService.search(email, pageable));
    }
    //PostMan Done
    @PutMapping("/{id}")
    public ResponseEntity<PassengerResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody PassengerRequestDTO request) {
        return ResponseEntity.ok(passengerService.update(id, request));
    }
    //PostMan Done
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        passengerService.delete(id);
        return ResponseEntity.noContent().build();
    }

    //PostMan Done
    @GetMapping("/{id}/bookings")
    public ResponseEntity<Page<BookingResponseDTO>> getBookingHistory(
            @PathVariable Long id,
            Pageable pageable) {
        return ResponseEntity.ok(
                bookingService.findByPassengerId(id, pageable));
    }
}
