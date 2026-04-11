package com.cinema.booking.services.impl;

import com.cinema.booking.services.SeatService;

import com.cinema.booking.dtos.SeatDTO;
import com.cinema.booking.entities.Room;
import com.cinema.booking.entities.Seat;
import com.cinema.booking.entities.SeatType;
import com.cinema.booking.repositories.RoomRepository;
import com.cinema.booking.repositories.SeatRepository;
import com.cinema.booking.repositories.SeatTypeRepository;
import com.cinema.booking.repositories.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SeatServiceImpl implements SeatService {

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private SeatTypeRepository seatTypeRepository;

    @Autowired
    private TicketRepository ticketRepository;

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
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Phong chieu khong ton tai."));

        Set<String> seenCodes = new HashSet<>();
        List<String> orderedCodes = new ArrayList<>();
        for (SeatDTO dto : seatDTOs) {
            String code = resolveSeatCode(dto);
            if (!seenCodes.add(code)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trung ma ghe trong so do: " + code);
            }
            orderedCodes.add(code);
        }
        Set<String> desiredCodes = new HashSet<>(orderedCodes);

        List<Seat> existingSeats = seatRepository.findByRoom_RoomId(roomId);
        Map<String, Seat> byNormalizedCode = new HashMap<>();
        for (Seat s : existingSeats) {
            String key = normalizeSeatCode(s.getSeatCode());
            byNormalizedCode.put(key, s);
        }

        // Chi xoa ghe bo khoi so do; khong xoa neu con ve (FK tickets.seat_id)
        for (Seat seat : new ArrayList<>(existingSeats)) {
            String code = normalizeSeatCode(seat.getSeatCode());
            if (desiredCodes.contains(code)) {
                continue;
            }
            if (ticketRepository.countBySeat_SeatId(seat.getSeatId()) > 0) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Khong bo ghe " + code + ": da co ve gan voi ghe nay. Hay giu vi tri hoac xu ly ve truoc.");
            }
            seatRepository.delete(seat);
            byNormalizedCode.remove(code);
        }

        List<Seat> savedOrdered = new ArrayList<>();
        for (SeatDTO dto : seatDTOs) {
            String code = resolveSeatCode(dto);
            SeatType seatType = seatTypeRepository.findById(dto.getSeatTypeId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "SeatType khong hop le: " + dto.getSeatTypeId()));
            boolean active = dto.getIsActive() != null ? dto.getIsActive() : Boolean.TRUE;
            Seat seat = byNormalizedCode.get(code);
            if (seat != null) {
                seat.setSeatType(seatType);
                seat.setIsActive(active);
                savedOrdered.add(seatRepository.save(seat));
            } else {
                Seat created = new Seat();
                created.setRoom(room);
                created.setSeatCode(code);
                created.setSeatType(seatType);
                created.setIsActive(active);
                Seat persisted = seatRepository.save(created);
                byNormalizedCode.put(code, persisted);
                savedOrdered.add(persisted);
            }
        }
        return savedOrdered.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    private static String normalizeSeatCode(String seatCode) {
        return seatCode == null ? "" : seatCode.trim().toUpperCase();
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
