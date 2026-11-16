package org.carpooling.platform.repository;

import org.carpooling.platform.entity.StoppingPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoppingPointRepository extends JpaRepository<StoppingPoint, Long> {
}
