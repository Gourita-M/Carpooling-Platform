package org.carpooling.platform.repository;

import org.carpooling.platform.entity.Trip;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {
    Page<Trip> findByDriverId(Long driverId, Pageable pageable);
    Page<Trip> findByDepartureCityContainingIgnoreCaseOrDestinationCityContainingIgnoreCaseOrDepartureDateContainingIgnoreCaseOrSeatsOfferedContainingIgnoreCase(
            String departureCity,
            String destinationCity,
            LocalDate departureDate,
            Long numberOfSeats,
            Pageable pageable
    );
    Page<Trip>searchTrips(
            String departureCity,
            String destinationCity,
            LocalDate date,
            int numberOfSeats,
            BigDecimal maxPrice,
            Pageable pageable
            );
    Trip findByIdAndDriver_Id(Long tripId, Long driverId);
}
