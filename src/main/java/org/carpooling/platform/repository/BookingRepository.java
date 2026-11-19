package org.carpooling.platform.repository;

import org.carpooling.platform.entity.Booking;
import org.carpooling.platform.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    //int sumConfirmedSeatsByTripId(Long tripId);
    boolean existsByPassengerIdAndTripIdAndStatus(Long passId, Long tripId, BookingStatus status);
    Page<Booking> findByPassengerId(Long passengerId, Pageable pageable);
    Page<Booking> findByTripId(Long tripId, Pageable pageable);
}