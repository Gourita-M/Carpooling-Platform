package org.carpooling.platform.repository;

import org.carpooling.platform.entity.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PassengerRepository extends JpaRepository<Passenger, Long> {
    
}
