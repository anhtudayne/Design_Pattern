package com.cinema.booking.services;

import com.cinema.booking.dtos.LocationDTO;
import java.util.List;

public interface LocationService {
    List<LocationDTO> getAllLocations();
    LocationDTO getLocationById(Integer id);
    LocationDTO createLocation(LocationDTO locationDTO);
    LocationDTO updateLocation(Integer id, LocationDTO locationDTO);
    void deleteLocation(Integer id);
}
