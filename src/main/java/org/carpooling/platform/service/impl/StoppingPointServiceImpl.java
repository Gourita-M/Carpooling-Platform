package org.carpooling.platform.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.carpooling.platform.dto.request.StoppingPointRequestDTO;
import org.carpooling.platform.dto.response.StoppingPointResponseDTO;
import org.carpooling.platform.entity.StoppingPoint;
import org.carpooling.platform.exception.BusinessException;
import org.carpooling.platform.exception.ResourceNotFoundException;
import org.carpooling.platform.mapper.StoppingPointMapper;
import org.carpooling.platform.repository.StoppingPointRepository;
import org.carpooling.platform.repository.TripRepository;
import org.carpooling.platform.service.StoppingPointService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class StoppingPointServiceImpl implements StoppingPointService {

    private final StoppingPointRepository stoppingPointRepository;
    private final TripRepository tripRepository;
    private final StoppingPointMapper stoppingPointMapper;

    @Override
    public StoppingPointResponseDTO findById(Long id) {
    log.info("Getting StoppingPoint With ID: {}", id);
        StoppingPoint point = stoppingPointRepository.findById(id).orElseThrow(() -> {
            log.warn("Stopping Point ID: {}", id);
            return new IllegalArgumentException("Stopping point not found");
        });
        return stoppingPointMapper.toResponseDTO(point);
    }

    @Override
    public List<StoppingPointResponseDTO> findByTripId(Long tripId) {
    log.info("Getting Stopping Point With Trip ID: {}", tripId);
        if (!tripRepository.existsById(tripId)) {
            log.warn("Trip ID: {}, Can't Be Found", tripId);
            throw new ResourceNotFoundException("Trip not found");
        }
        return stoppingPointRepository
                .findByTripIdOrderByPointOrderAsc(tripId)
                .stream()
                .map(stoppingPointMapper::toResponseDTO)
                .toList();
    }

    @Override
    public void validatePoints(List<StoppingPointRequestDTO> points) {
    log.info("Validating Stopping points");
        if (points == null || points.size() < 2) {
            log.warn("Trip Can't Have < 2 Stopping Points");
            throw new BusinessException("A trip must have at least two stopping points");
        }

        Set<String> cities = new HashSet<>();

        for (int i = 0; i < points.size(); i++) {

            StoppingPointRequestDTO current = points.get(i);

            if (current.city() == null || current.city().isBlank()) {
                log.warn("City field Can't Be Empty");
                throw new BusinessException("City cannot be empty");
            }

            String normalizedCity = current.city().trim().toLowerCase();

            if (!cities.add(normalizedCity)) {
                log.warn("City Deduplicated");
                throw new BusinessException("A city cannot appear twice in the same trip");
            }

            if (current.pointOrder() != i) {
                log.warn("There is No Starting Point");
                throw new BusinessException("Stopping point order must start at 0 and be consecutive");
            }

            if (current.passingTime() == null || current.priceFromDeparture() == null) {
                log.warn("Passing Time and Price Fields Are Empty");
                throw new IllegalArgumentException("Passing time and price are required");
            }

            if (current.priceFromDeparture().signum() < 0) {
                log.warn("Price Can't be Negative");
                throw new IllegalArgumentException("Price cannot be negative");
            }

            if (i == 0) {

                if (!current.passingTime().isAfter(LocalDateTime.now())) {
                    log.warn("Departure Is In the Past");
                    throw new IllegalArgumentException("Departure must be in the future");
                }

                if (current.priceFromDeparture().compareTo(BigDecimal.ZERO) != 0) {
                    log.warn("Departure Point Price != 0");
                    throw new IllegalArgumentException("Departure point price must be zero");
                }

            } else {

                StoppingPointRequestDTO previous = points.get(i - 1);

                if (!current.passingTime().isAfter(previous.passingTime())) {
                    log.warn("Passing Time Not increasing");
                    throw new IllegalArgumentException("Passing times must be strictly increasing");
                }

                if (current.priceFromDeparture().compareTo(previous.priceFromDeparture()) <= 0) {
                    log.warn("Prices Not increase");
                    throw new IllegalArgumentException("Prices must strictly increase");
                }
            }
        }
    }
}
