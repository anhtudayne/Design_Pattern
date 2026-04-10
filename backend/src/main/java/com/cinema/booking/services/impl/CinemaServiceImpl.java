package com.cinema.booking.services.impl;

import com.cinema.booking.services.CinemaService;

import com.cinema.booking.dtos.CinemaDTO;
import com.cinema.booking.entities.Cinema;
import com.cinema.booking.entities.Location;
import com.cinema.booking.repositories.CinemaRepository;
import com.cinema.booking.repositories.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CinemaServiceImpl implements CinemaService {

    @Autowired
    private CinemaRepository cinemaRepository;

    @Autowired
    private LocationRepository locationRepository;

    private CinemaDTO mapToDTO(Cinema cinema) {
        CinemaDTO dto = new CinemaDTO();
        dto.setCinemaId(cinema.getCinemaId());
        
        if (cinema.getLocation() != null) {
            dto.setLocationId(cinema.getLocation().getLocationId());
            dto.setLocationName(cinema.getLocation().getName());
        }
        
        dto.setName(cinema.getName());
        dto.setAddress(cinema.getAddress());
        dto.setHotline(cinema.getHotline());
        return dto;
    }

    @Override
    public List<CinemaDTO> getAllCinemas() {
        return cinemaRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public List<CinemaDTO> getCinemasByLocation(Integer locationId) {
        return cinemaRepository.findByLocation_LocationId(locationId).stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public CinemaDTO getCinemaById(Integer id) {
        Cinema cinema = cinemaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy Cụm Rạp này!"));
        return mapToDTO(cinema);
    }

    @Override
    public CinemaDTO createCinema(CinemaDTO dto) {
        Cinema cinema = new Cinema();
        Location location = locationRepository.findById(dto.getLocationId())
            .orElseThrow(() -> new RuntimeException("Tỉnh/Thành phố chọn không hợp lệ!"));
        
        cinema.setLocation(location);
        cinema.setName(dto.getName());
        cinema.setAddress(dto.getAddress());
        cinema.setHotline(dto.getHotline());
        
        return mapToDTO(cinemaRepository.save(cinema));
    }

    @Override
    public CinemaDTO updateCinema(Integer id, CinemaDTO dto) {
        Cinema cinema = cinemaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy Cụm Rạp này để sửa!"));
            
        Location location = locationRepository.findById(dto.getLocationId())
            .orElseThrow(() -> new RuntimeException("Tỉnh/Thành phố đổi sang không hợp lệ!"));

        cinema.setLocation(location);
        cinema.setName(dto.getName());
        cinema.setAddress(dto.getAddress());
        cinema.setHotline(dto.getHotline());
        
        return mapToDTO(cinemaRepository.save(cinema));
    }

    @Override
    public void deleteCinema(Integer id) {
        cinemaRepository.deleteById(id);
    }
}
