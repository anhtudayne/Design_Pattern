package com.cinema.booking.services.impl;

import com.cinema.booking.services.SeatService;

import com.cinema.booking.dtos.SeatDTO;
import com.cinema.booking.entities.Room;
import com.cinema.booking.entities.Seat;
import com.cinema.booking.entities.SeatType;
import com.cinema.booking.repositories.RoomRepository;
import com.cinema.booking.repositories.SeatRepository;
import com.cinema.booking.repositories.SeatTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SeatServiceImpl implements SeatService {

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private SeatTypeRepository seatTypeRepository;

    private SeatDTO mapToDTO(Seat seat) {
        SeatDTO dto = new SeatDTO();
        dto.setSeatId(seat.getSeatId());
        
        if (seat.getRoom() != null) {
            dto.setRoomId(seat.getRoom().getRoomId());
        }
        
        dto.setSeatCode(seat.getSeatCode());
        String seatCode = seat.getSeatCode() == null ? "" : seat.getSeatCode().trim();
        if (seatCode.length() >= 2) {
            dto.setSeatRow(seatCode.substring(0, 1));
            try {
                dto.setSeatNumber(Integer.parseInt(seatCode.substring(1)));
            } catch (NumberFormatException ignored) {
                dto.setSeatNumber(null);
            }
        }
        dto.setIsActive(seat.getIsActive() != null ? seat.getIsActive() : Boolean.TRUE);
        if (seat.getSeatType() != null) {
            dto.setSeatTypeId(seat.getSeatType().getId());
            dto.setSeatTypeName(seat.getSeatType().getName());
            dto.setSeatTypeSurcharge(seat.getSeatType().getPriceSurcharge());
        }
        return dto;
    }

    @Override
    public List<SeatDTO> getAllSeats() {
        return seatRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public List<SeatDTO> getSeatsByRoom(Integer roomId) {
        return seatRepository.findByRoom_RoomId(roomId).stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public SeatDTO getSeatById(Integer id) {
        Seat seat = seatRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy Ghế này (Mã ID hỏng)!"));
        return mapToDTO(seat);
    }

    @Override
    public SeatDTO createSeat(SeatDTO dto) {
        Seat seat = new Seat();
        Room room = roomRepository.findById(dto.getRoomId())
            .orElseThrow(() -> new RuntimeException("Mã Phòng chiếu chặn nối Ghế không hợp lệ!"));

        SeatType seatType = seatTypeRepository.findById(dto.getSeatTypeId())
                .orElseThrow(() -> new RuntimeException("SeatType không hợp lệ: " + dto.getSeatTypeId()));
        
        seat.setRoom(room);
        seat.setSeatCode(resolveSeatCode(dto));
        seat.setSeatType(seatType);
        
        return mapToDTO(seatRepository.save(seat));
    }

    @Override
    public SeatDTO updateSeat(Integer id, SeatDTO dto) {
        Seat seat = seatRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Ghế không tồn tại trên hệ thống để sửa!"));
            
        Room room = roomRepository.findById(dto.getRoomId())
            .orElseThrow(() -> new RuntimeException("Mã Phòng chiếu chỉ định đè bản vá không hợp lệ!"));

        SeatType seatType = seatTypeRepository.findById(dto.getSeatTypeId())
                .orElseThrow(() -> new RuntimeException("SeatType không hợp lệ: " + dto.getSeatTypeId()));

        seat.setRoom(room);
        seat.setSeatCode(resolveSeatCode(dto));
        seat.setSeatType(seatType);
        
        return mapToDTO(seatRepository.save(seat));
    }

    @Override
    public void deleteSeat(Integer id) {
        seatRepository.deleteById(id);
    }

    @Override
    @Transactional
    public List<SeatDTO> replaceAllSeatsInRoom(Integer roomId, List<SeatDTO> seatDTOs) {
        Room room = roomRepository.findById(roomId)
            .orElseThrow(() -> new RuntimeException("Phòng chiếu không tồn tại!"));

        // 1. Xóa toàn bộ ghế cũ của phòng
        List<Seat> existingSeats = seatRepository.findByRoom_RoomId(roomId);
        seatRepository.deleteAll(existingSeats);
        seatRepository.flush();

        // 2. Tạo toàn bộ ghế mới
        List<Seat> newSeats = new ArrayList<>();
        for (SeatDTO dto : seatDTOs) {
            SeatType seatType = seatTypeRepository.findById(dto.getSeatTypeId())
                    .orElseThrow(() -> new RuntimeException("SeatType không hợp lệ: " + dto.getSeatTypeId()));
            Seat seat = new Seat();
            seat.setRoom(room);
            seat.setSeatCode(resolveSeatCode(dto));
            seat.setSeatType(seatType);
            seat.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
            newSeats.add(seat);
        }
        List<Seat> savedSeats = seatRepository.saveAll(newSeats);
        return savedSeats.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    private String resolveSeatCode(SeatDTO dto) {
        if (dto.getSeatCode() != null && !dto.getSeatCode().isBlank()) {
            return dto.getSeatCode().trim().toUpperCase();
        }
        if (dto.getSeatRow() != null && dto.getSeatNumber() != null) {
            return (dto.getSeatRow().trim() + dto.getSeatNumber()).toUpperCase();
        }
        throw new RuntimeException("Thiếu seatCode hoặc seatRow/seatNumber hợp lệ");
    }
}
