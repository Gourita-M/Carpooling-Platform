package org.carpooling.platform.repository;

import org.carpooling.platform.entity.Driver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {

    Page<Driver> findByFirstNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String firstName,
            String email,
            Pageable pageable
    );
}
