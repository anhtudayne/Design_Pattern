package com.cinema.booking.services;

import com.cinema.booking.dtos.RoomDTO;
import java.util.List;

public interface RoomService {
    List<RoomDTO> getAllRooms();
    List<RoomDTO> getRoomsByCinema(Integer cinemaId);
    RoomDTO getRoomById(Integer id);
    RoomDTO createRoom(RoomDTO roomDTO);
    RoomDTO updateRoom(Integer id, RoomDTO roomDTO);
    void deleteRoom(Integer id);
}
