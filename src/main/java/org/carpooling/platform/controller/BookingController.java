package org.carpooling.platform.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.carpooling.platform.dto.request.BookingRequestDTO;
import org.carpooling.platform.dto.response.BookingResponseDTO;
import org.carpooling.platform.service.BookingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;


@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    //PostMan Done
    @PostMapping
    public ResponseEntity<BookingResponseDTO> create(
            @Valid @RequestBody BookingRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookingService.create(request));
    }

    //PostMan Done
    @GetMapping("/{id}")
    public ResponseEntity<BookingResponseDTO> findById(
            @PathVariable Long id) {
        return ResponseEntity.ok(bookingService.findById(id));
    }

    //PostMan Done
    @GetMapping("/passenger/{passengerId}")
    public ResponseEntity<Page<BookingResponseDTO>> findByPassengerId(
            @PathVariable Long passengerId,
            Pageable pageable) {
        return ResponseEntity.ok(
                bookingService.findByPassengerId(passengerId, pageable));
    }
    //PostMan Done
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<BookingResponseDTO> cancel(
            @PathVariable Long id,
            @RequestParam Long passengerId) {
        return ResponseEntity.ok(
                bookingService.cancel(id, passengerId));
    }

    //PostMan Done
    @GetMapping("/calculate-price")
    public ResponseEntity<BigDecimal> calculatePrice(
            @RequestParam Long pickUpPointId,
            @RequestParam Long dropOffPointId,
            @RequestParam int numberOfSeats) {

        return ResponseEntity.ok(
                bookingService.calculatePrice(
                        pickUpPointId,
                        dropOffPointId,
                        numberOfSeats));
    }
}
