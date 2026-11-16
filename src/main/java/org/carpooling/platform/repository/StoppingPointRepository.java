package org.carpooling.platform.repository;

import org.carpooling.platform.entity.StoppingPoint;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoppingPointRepository extends JpaRepository<StoppingPoint, Long> {
}
