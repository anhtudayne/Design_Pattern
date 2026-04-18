package com.cinema.booking.service;

import com.cinema.booking.dto.RoomDTO;
import java.util.List;

public interface RoomService {
    List<RoomDTO> getAllRooms();
    List<RoomDTO> getRoomsByCinema(Integer cinemaId);
    RoomDTO getRoomById(Integer id);
    RoomDTO createRoom(RoomDTO roomDTO);
    RoomDTO updateRoom(Integer id, RoomDTO roomDTO);
    void deleteRoom(Integer id);
}
