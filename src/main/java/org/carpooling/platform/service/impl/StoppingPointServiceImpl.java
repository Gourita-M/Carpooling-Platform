package org.carpooling.platform.service.impl;

import lombok.RequiredArgsConstructor;
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
public class StoppingPointServiceImpl implements StoppingPointService {

    private final StoppingPointRepository stoppingPointRepository;
    private final TripRepository tripRepository;
    private final StoppingPointMapper stoppingPointMapper;

    @Override
    public StoppingPointResponseDTO findById(Long id) {

        StoppingPoint point = stoppingPointRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Stopping point not found"));

        return stoppingPointMapper.toResponseDTO(point);
    }

    @Override
    public List<StoppingPointResponseDTO> findByTripId(Long tripId) {

        if (!tripRepository.existsById(tripId)) {
            throw new ResourceNotFoundException("Trip not found");
        }

        return stoppingPointRepository
                .findByTripIdOrderByPointOrderAsc(tripId)
                .stream()
                .map(stoppingPointMapper::toResponseDTO)
                .toList();
    }

    @Override
    public void validatePoints(
            List<StoppingPointRequestDTO> points) {

        if (points == null || points.size() < 2) {
            throw new BusinessException(
                    "A trip must have at least two stopping points");
        }

        Set<String> cities = new HashSet<>();

        for (int i = 0; i < points.size(); i++) {

            StoppingPointRequestDTO current = points.get(i);

            if (current.city() == null || current.city().isBlank()) {
                throw new BusinessException("City cannot be empty");
            }

            String normalizedCity =
                    current.city().trim().toLowerCase();

            if (!cities.add(normalizedCity)) {
                throw new BusinessException(
                        "A city cannot appear twice in the same trip");
            }

            if (current.pointOrder() != i) {
                throw new BusinessException(
                        "Stopping point order must start at 0 and be consecutive");
            }

            if (current.passingTime() == null ||
                    current.priceFromDeparture() == null) {
                throw new IllegalArgumentException(
                        "Passing time and price are required");
            }

            if (current.priceFromDeparture().signum() < 0) {
                throw new IllegalArgumentException(
                        "Price cannot be negative");
            }

            if (i == 0) {

                if (!current.passingTime().isAfter(LocalDateTime.now())) {
                    throw new IllegalArgumentException(
                            "Departure must be in the future");
                }

                if (current.priceFromDeparture()
                        .compareTo(BigDecimal.ZERO) != 0) {
                    throw new IllegalArgumentException(
                            "Departure point price must be zero");
                }

            } else {

                StoppingPointRequestDTO previous = points.get(i - 1);

                if (!current.passingTime()
                        .isAfter(previous.passingTime())) {
                    throw new IllegalArgumentException(
                            "Passing times must be strictly increasing");
                }

                if (current.priceFromDeparture()
                        .compareTo(previous.priceFromDeparture()) <= 0) {
                    throw new IllegalArgumentException(
                            "Prices must strictly increase");
                }
            }
        }
    }
}
