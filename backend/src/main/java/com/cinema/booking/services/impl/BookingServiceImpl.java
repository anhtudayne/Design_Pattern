package com.cinema.booking.services.impl;

import com.cinema.booking.dtos.BookingCalculationDTO;
import com.cinema.booking.dtos.BookingDTO;
import com.cinema.booking.dtos.PriceBreakdownDTO;
import com.cinema.booking.dtos.SeatStatusDTO;
import com.cinema.booking.domain.seat.SeatState;
import com.cinema.booking.domain.seat.SeatStateFactory;
import com.cinema.booking.entities.*;
import com.cinema.booking.repositories.*;
import com.cinema.booking.services.BookingService;
import com.cinema.booking.services.DynamicPricingService;
import com.cinema.booking.services.seatlock.SeatLockProvider;
import com.cinema.booking.services.strategy_decorator.pricing.PricingContext;
import com.cinema.booking.services.strategy_decorator.pricing.PricingEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    @Autowired
    private SeatLockProvider seatLockProvider;

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

    @Autowired
    private PricingEngine pricingEngine;

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

        List<Integer> seatIdsInOrder = allSeats.stream().map(Seat::getSeatId).collect(Collectors.toList());
        List<Boolean> lockHeld = seatLockProvider.batchLockHeld(showtimeId, seatIdsInOrder);

        return IntStream.range(0, allSeats.size()).mapToObj(i -> {
            Seat seat = allSeats.get(i);
            boolean sold = soldSeatIds.contains(seat.getSeatId());
            boolean held = i < lockHeld.size() && Boolean.TRUE.equals(lockHeld.get(i));
            SeatState state = SeatStateFactory.fromSnapshot(sold, held);
            SeatStatusDTO.SeatStatus status = state.toDisplayStatus();

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
        boolean isSold = ticketRepository.findByShowtime_ShowtimeId(showtimeId).stream()
                .anyMatch(t -> t.getSeat().getSeatId().equals(seatId));
        SeatState state = SeatStateFactory.fromSnapshot(isSold, false);
        if (!state.allowsLockAttempt()) {
            return false;
        }
        return seatLockProvider.tryAcquire(showtimeId, seatId, userId, redisTtlSeconds);
    }

    @Override
    public void unlockSeat(Integer showtimeId, Integer seatId) {
        seatLockProvider.release(showtimeId, seatId);
    }

    @Override
    public PriceBreakdownDTO calculatePrice(BookingCalculationDTO request) {
        Showtime showtime = showtimeRepository.findById(request.getShowtimeId())
                .orElseThrow(() -> new RuntimeException("Suất chiếu không tồn tại!"));

        List<Seat> seats = seatRepository.findAllById(request.getSeatIds());
        Promotion promotion = (request.getPromoCode() != null && !request.getPromoCode().isBlank())
                ? promotionRepository.findByCode(request.getPromoCode()).orElse(null)
                : null;

        return pricingEngine.calculateTotalPrice(
                PricingContext.builder()
                        .showtime(showtime)
                        .seats(seats)
                        .fnbs(request.getFnbs())
                        .promotion(promotion)
                        .build()
        );
    }

    @Override
    public BookingDTO getBookingDetail(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Booking ID: " + bookingId));

        return mapToDTO(booking);
    }

    @Override
    public List<BookingDTO> searchBookings(String query) {
        return bookingRepository.findAll(
                com.cinema.booking.patterns.specification.BookingSpecificationBuilder.searchBookings(query)
        ).stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void cancelBooking(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new RuntimeException("Booking not found"));
        com.cinema.booking.patterns.state.BookingContext context = new com.cinema.booking.patterns.state.BookingContext(booking);
        context.cancel();
        bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public void refundBooking(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new RuntimeException("Booking not found"));
        com.cinema.booking.patterns.state.BookingContext context = new com.cinema.booking.patterns.state.BookingContext(booking);
        context.refund();
        bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public void printTickets(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new RuntimeException("Booking not found"));
        com.cinema.booking.patterns.state.BookingContext context = new com.cinema.booking.patterns.state.BookingContext(booking);
        context.printTickets();
        // Maybe update DB if printing changes a specific physical flag, for now state pattern enforces rules.
    }

    private BookingDTO mapToDTO(Booking booking) {
        List<Ticket> tickets = ticketRepository.findByBooking_BookingId(booking.getBookingId());
        List<FnBLine> fnbs = fnBLineRepository.findByBooking_BookingId(booking.getBookingId());

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
