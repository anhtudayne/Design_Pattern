package com.cinema.booking.services;

import com.cinema.booking.dtos.SeatDTO;
import java.util.List;

public interface SeatService {
    List<SeatDTO> getAllSeats();
    List<SeatDTO> getSeatsByRoom(Integer roomId);
    SeatDTO getSeatById(Integer id);
    SeatDTO createSeat(SeatDTO seatDTO);
    SeatDTO updateSeat(Integer id, SeatDTO seatDTO);
    void deleteSeat(Integer id);
    List<SeatDTO> replaceAllSeatsInRoom(Integer roomId, List<SeatDTO> seatDTOs);
}
