package com.cinema.booking.service.impl;

import com.cinema.booking.service.LocationService;

import com.cinema.booking.dto.LocationDTO;
import com.cinema.booking.entity.Location;
import com.cinema.booking.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LocationServiceImpl implements LocationService {
    
    @Autowired
    private LocationRepository locationRepository;

    private LocationDTO mapToDTO(Location location) {
        LocationDTO dto = new LocationDTO();
        dto.setId(location.getId());
        dto.setName(location.getName());
        return dto;
    }

    @Override
    public List<LocationDTO> getAllLocations() {
        return locationRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public LocationDTO getLocationById(Integer id) {
        Location location = locationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy Tỉnh/Thành phố này!"));
        return mapToDTO(location);
    }

    @Override
    public LocationDTO createLocation(LocationDTO locationDTO) {
        Location location = new Location();
        location.setName(locationDTO.getName()); // Map Data
        return mapToDTO(locationRepository.save(location)); // Lưu Database và trả về DTO
    }

    @Override
    public LocationDTO updateLocation(Integer id, LocationDTO locationDTO) {
        Location location = locationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy Tỉnh/Thành phố này!"));
        location.setName(locationDTO.getName());
        return mapToDTO(locationRepository.save(location));
    }

    @Override
    public void deleteLocation(Integer id) {
        locationRepository.deleteById(id);
    }
}
