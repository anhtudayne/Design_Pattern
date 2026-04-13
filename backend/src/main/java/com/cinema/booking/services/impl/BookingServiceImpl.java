package com.cinema.booking.services.impl;

import com.cinema.booking.dtos.BookingCalculationDTO;
import com.cinema.booking.dtos.BookingDTO;
import com.cinema.booking.dtos.PriceBreakdownDTO;
import com.cinema.booking.dtos.SeatStatusDTO;
import com.cinema.booking.entities.*;
import com.cinema.booking.repositories.*;
import com.cinema.booking.services.BookingService;
import com.cinema.booking.services.DynamicPricingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements BookingService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ShowtimeRepository showtimeRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private TicketRepository ticketRepository;

    /** @Primary → Spring inject CachingDynamicPricingProxy tự động */
    @Autowired
    private DynamicPricingService dynamicPricingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private FnBLineRepository fnBLineRepository;

    @Value("${cinema.app.redisTtlSeconds:600}")
    private long redisTtlSeconds;

    private String getLockKey(Integer showtimeId, Integer seatId) {
        return "showtime:" + showtimeId + ":seat:" + seatId + ":lock";
    }

    @Override
    public List<SeatStatusDTO> getSeatStatuses(Integer showtimeId) {
        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy suất chiếu!"));

        // 1. Lấy toàn bộ ghế trong phòng (query trực tiếp — không dùng findAll)
        List<Seat> allSeats = seatRepository.findByRoom_RoomId(showtime.getRoom().getRoomId());

        // 2. Lấy danh sách vé đã bán cho suất này
        Set<Integer> soldSeatIds = ticketRepository.findByShowtime_ShowtimeId(showtimeId).stream()
                .map(t -> t.getSeat().getSeatId())
                .collect(Collectors.toSet());

        // 3. TỐI ƯU REDIS: Lấy toàn bộ khóa lock trong 1 lần gọi duy nhất (tránh vòng lặp gọi N lần)
        List<String> lockKeys = allSeats.stream()
                .map(seat -> getLockKey(showtimeId, seat.getSeatId()))
                .collect(Collectors.toList());
        List<Object> locks = redisTemplate.opsForValue().multiGet(lockKeys);

        return allSeats.stream().map(seat -> {
            SeatStatusDTO.SeatStatus status = SeatStatusDTO.SeatStatus.VACANT;
            
            int index = allSeats.indexOf(seat);
            boolean isLocked = locks != null && locks.get(index) != null;

            if (soldSeatIds.contains(seat.getSeatId())) {
                status = SeatStatusDTO.SeatStatus.SOLD;
            } else if (isLocked) {
                status = SeatStatusDTO.SeatStatus.PENDING;
            }

            BigDecimal totalPrice = showtime.getBasePrice()
                    .add(seat.getSeatType() != null && seat.getSeatType().getPriceSurcharge() != null
                            ? seat.getSeatType().getPriceSurcharge()
                            : BigDecimal.ZERO);

            return SeatStatusDTO.builder()
                    .seatId(seat.getSeatId())
                    .seatCode(seat.getSeatCode())
                    .seatRow(extractSeatRow(seat.getSeatCode()))
                    .seatNumber(extractSeatNumber(seat.getSeatCode()))
                    .seatType(seat.getSeatType() != null ? seat.getSeatType().getName() : "STANDARD")
                    .totalPrice(totalPrice)
                    .status(status)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public boolean lockSeat(Integer showtimeId, Integer seatId, Integer userId) {
        String key = getLockKey(showtimeId, seatId);
        // Kiểm tra xem ghế đã bán chưa
        boolean isSold = ticketRepository.findByShowtime_ShowtimeId(showtimeId).stream()
                .anyMatch(t -> t.getSeat().getSeatId().equals(seatId));
        if (isSold) return false;

        // Thử đặt khóa trong Redis (SETNX)
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, userId, redisTtlSeconds, TimeUnit.SECONDS));
    }

    @Override
    public void unlockSeat(Integer showtimeId, Integer seatId) {
        redisTemplate.delete(getLockKey(showtimeId, seatId));
    }

    @Override
    public PriceBreakdownDTO calculatePrice(BookingCalculationDTO request) {
        return dynamicPricingService.calculatePrice(request);
    }

    @Override
    public BookingDTO getBookingDetail(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Booking ID: " + bookingId));

        List<Ticket> tickets = ticketRepository.findByBooking_BookingId(bookingId);
        List<FnBLine> fnbs = fnBLineRepository.findByBooking_BookingId(bookingId);

        return BookingDTO.builder()
                .bookingId(booking.getBookingId())
                .customerId(booking.getCustomer() != null ? booking.getCustomer().getUserId() : null)
                .showtimeId(tickets.isEmpty() || tickets.get(0).getShowtime() == null ? null : tickets.get(0).getShowtime().getShowtimeId())
                .promoCode(booking.getPromotion() != null ? booking.getPromotion().getCode() : null)
                .totalPrice(
                        tickets.stream()
                                .map(Ticket::getPrice)
                                .filter(java.util.Objects::nonNull)
                                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add)
                                .add(
                                        fnbs.stream()
                                                .map(l -> l.getUnitPrice().multiply(java.math.BigDecimal.valueOf(l.getQuantity())))
                                                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add)
                                )
                )
                .status(booking.getStatus())
                .createdAt(booking.getCreatedAt())
                .tickets(tickets.stream().map(t -> BookingDTO.TicketLineDTO.builder()
                        .ticketId(t.getTicketId())
                        .seatId(t.getSeat() != null ? t.getSeat().getSeatId() : null)
                        .seatCode(t.getSeat() != null ? t.getSeat().getSeatCode() : null)
                        .seatRow(t.getSeat() != null ? extractSeatRow(t.getSeat().getSeatCode()) : null)
                        .seatNumber(t.getSeat() != null ? extractSeatNumber(t.getSeat().getSeatCode()) : null)
                        .seatType(t.getSeat() != null && t.getSeat().getSeatType() != null ? t.getSeat().getSeatType().getName() : null)
                        .seatSurcharge(t.getSeat() != null && t.getSeat().getSeatType() != null ? t.getSeat().getSeatType().getPriceSurcharge() : null)
                        .price(t.getPrice())
                        .build()).toList())
                .fnbs(fnbs.stream().map(l -> {
                    java.math.BigDecimal lineTotal = l.getUnitPrice().multiply(java.math.BigDecimal.valueOf(l.getQuantity()));
                    return BookingDTO.FnBLineDTO.builder()
                            .id(l.getId())
                            .itemId(l.getItem() != null ? l.getItem().getItemId() : null)
                            .itemName(l.getItem() != null ? l.getItem().getName() : null)
                            .quantity(l.getQuantity())
                            .unitPrice(l.getUnitPrice())
                            .lineTotal(lineTotal)
                            .build();
                }).toList())
                .build();
    }

    private String extractSeatRow(String seatCode) {
        if (seatCode == null || seatCode.isBlank()) return null;
        return seatCode.substring(0, 1);
    }

    private Integer extractSeatNumber(String seatCode) {
        if (seatCode == null || seatCode.length() < 2) return null;
        try {
            return Integer.parseInt(seatCode.substring(1));
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
