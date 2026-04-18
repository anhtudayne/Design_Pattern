package com.cinema.booking.service.impl;

import com.cinema.booking.service.RoomService;

import com.cinema.booking.dto.RoomDTO;
import com.cinema.booking.entity.Cinema;
import com.cinema.booking.entity.Room;
import com.cinema.booking.repository.CinemaRepository;
import com.cinema.booking.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomServiceImpl implements RoomService {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private CinemaRepository cinemaRepository;

    private RoomDTO mapToDTO(Room room) {
        RoomDTO dto = new RoomDTO();
        dto.setRoomId(room.getRoomId());
        
        if (room.getCinema() != null) {
            dto.setCinemaId(room.getCinema().getCinemaId());
        }
        
        dto.setName(room.getName());
        dto.setScreenType(room.getScreenType());
        return dto;
    }

    @Override
    public List<RoomDTO> getAllRooms() {
        return roomRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public List<RoomDTO> getRoomsByCinema(Integer cinemaId) {
        return roomRepository.findByCinema_CinemaId(cinemaId).stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public RoomDTO getRoomById(Integer id) {
        Room room = roomRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy Phòng chiếu này!"));
        return mapToDTO(room);
    }

    @Override
    public RoomDTO createRoom(RoomDTO dto) {
        Room room = new Room();
        Cinema cinema = cinemaRepository.findById(dto.getCinemaId())
            .orElseThrow(() -> new RuntimeException("Cụm rạp không hợp lệ để gắn Phòng!"));
        
        room.setCinema(cinema);
        room.setName(dto.getName());
        room.setScreenType(dto.getScreenType());
        
        return mapToDTO(roomRepository.save(room));
    }

    @Override
    public RoomDTO updateRoom(Integer id, RoomDTO dto) {
        Room room = roomRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy Phòng chiếu để sửa!"));
            
        Cinema cinema = cinemaRepository.findById(dto.getCinemaId())
            .orElseThrow(() -> new RuntimeException("Cụm rạp mới định chuyển sang không hợp lệ!"));

        room.setCinema(cinema);
        room.setName(dto.getName());
        room.setScreenType(dto.getScreenType());
        
        return mapToDTO(roomRepository.save(room));
    }

    @Override
    public void deleteRoom(Integer id) {
        roomRepository.deleteById(id);
    }
}
