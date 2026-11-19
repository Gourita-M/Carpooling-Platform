package org.carpooling.platform.repository;

import org.carpooling.platform.entity.Booking;
import org.carpooling.platform.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("""
        SELECT COALESCE(SUM(b.numberOfSeats), 0)
        FROM Booking b
        WHERE b.trip.id = :tripId
          AND b.status = :status
    """)
    Long sumConfirmedSeatsByTripId(
            @Param("tripId") Long tripId,
            @Param("status") BookingStatus status
    );
    boolean existsByTripId(Long tripId);
    boolean existsByPassengerIdAndTripIdAndStatus(Long passId, Long tripId, BookingStatus status);
    Page<Booking> findByPassengerId(Long passengerId, Pageable pageable);
    Page<Booking> findByTripId(Long tripId, Pageable pageable);
    List<Booking> findByTripIdAndStatus(Long tripId, BookingStatus status);
}