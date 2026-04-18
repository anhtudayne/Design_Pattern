package com.cinema.booking.service;

import com.cinema.booking.dto.LocationDTO;
import java.util.List;

public interface LocationService {
    List<LocationDTO> getAllLocations();
    LocationDTO getLocationById(Integer id);
    LocationDTO createLocation(LocationDTO locationDTO);
    LocationDTO updateLocation(Integer id, LocationDTO locationDTO);
    void deleteLocation(Integer id);
}
