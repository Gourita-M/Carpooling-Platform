package org.carpooling.platform.service;

import org.carpooling.platform.dto.request.StoppingPointRequestDTO;
import org.carpooling.platform.dto.response.StoppingPointResponseDTO;
import org.carpooling.platform.repository.StoppingPointRepository;

public class StoppingPointServiceImpl implements StoppingPointService{
    private final StoppingPointRepository repository;

    public StoppingPointServiceImpl(StoppingPointRepository repository) {
        this.repository = repository;
    }

    @Override
    public StoppingPointResponseDTO create(StoppingPointRequestDTO dto){
        return null;
    }

}
