package org.carpooling.platform.repository;

import org.carpooling.platform.entity.Passenger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PassengerRepository extends JpaRepository<Passenger, Long> {
    Page<Passenger> findByEmailContainingIgnoreCase(String email, Pageable pageable);
}
