package org.carpooling.platform.service;

import org.carpooling.platform.dto.request.VehicleRequestDTO;
import org.carpooling.platform.dto.response.VehicleResponseDTO;
import org.carpooling.platform.repository.VehicleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VehicleServiceImpl implements VehicleService{
    private final VehicleRepository repository;

    public VehicleServiceImpl(VehicleRepository repository) {
        this.repository = repository;
    }

    @Override
    public VehicleResponseDTO create(VehicleRequestDTO dto){
        return null;
    }

    @Override
    public VehicleResponseDTO findById(Long id){
        return null;
    }

    @Override
    public List<VehicleResponseDTO> findByDriverId(Long driverId){
        return null;
    }

    @Override
    public VehicleResponseDTO update(Long vehicleId, VehicleRequestDTO request){
        return null;
    }

    @Override
    public void delete(Long vehicleId){

    }
}
