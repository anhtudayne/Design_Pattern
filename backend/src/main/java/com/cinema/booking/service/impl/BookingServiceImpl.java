package com.cinema.booking.service.impl;
import com.cinema.booking.pattern.state.booking.BookingContext;

import com.cinema.booking.dto.BookingCalculationDTO;
import com.cinema.booking.dto.BookingDTO;
import com.cinema.booking.dto.PriceBreakdownDTO;
import com.cinema.booking.dto.SeatStatusDTO;
import com.cinema.booking.pattern.state.seat.SeatState;
import com.cinema.booking.pattern.state.seat.SeatStateFactory;
import com.cinema.booking.entity.*;
import com.cinema.booking.repository.*;
import com.cinema.booking.service.BookingService;
import com.cinema.booking.adapter.seatlock.SeatLockProvider;
import com.cinema.booking.pattern.strategy.pricing.PricingContext;
import com.cinema.booking.pattern.decorator.PricingEngine;
import com.cinema.booking.pattern.chain.PricingValidationContext;
import com.cinema.booking.pattern.chain.PricingValidationHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@Slf4j
public class BookingServiceImpl implements BookingService {

    @Autowired
    private SeatLockProvider seatLockProvider;

    @Autowired
    private ShowtimeRepository showtimeRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private FnBLineRepository fnBLineRepository;

    @Autowired
    private PaymentRepository paymentRepository;


    @Value("${cinema.app.redisTtlSeconds:600}")
    private long redisTtlSeconds;

    @Autowired
    @Qualifier("cachingPricingEngineProxy")
    private PricingEngine pricingEngine;

    @Autowired
    @Qualifier("pricingValidationChain")
    private PricingValidationHandler pricingValidationChain;

    @Autowired
    

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
        List<Boolean> lockHeldTemp;
        try {
            lockHeldTemp = seatLockProvider.batchLockHeld(showtimeId, seatIdsInOrder);
        } catch (Exception ex) {
            log.warn("Redis lock check failed for showtime {}. Continue without lock data: {}",
                    showtimeId, ex.getMessage());
            lockHeldTemp = java.util.Collections.emptyList();
        }
        final List<Boolean> lockHeld = lockHeldTemp;

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
        PricingValidationContext validationCtx = PricingValidationContext.builder()
                .request(request)
                .build();

        pricingValidationChain.validate(validationCtx);

        // Promotion validation is handled in validation chain, 
        // inventory validation is removed per new logic schema

        // TODO: Build PricingContext from DTO
        return pricingEngine.calculateTotalPrice(null);
    }


    @Override
    public BookingDTO getBookingDetail(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Booking ID: " + bookingId));

        return mapToDTO(booking);
    }

    @Override
    public List<BookingDTO> searchBookings(String query) {
        String q = query == null ? "" : query.trim().toLowerCase();
        return bookingRepository.findAll().stream()
                .filter(b -> q.isEmpty()
                        || (b.getBookingCode() != null && b.getBookingCode().toLowerCase().contains(q))
                        || (b.getUser() != null && b.getUser().getFullname() != null
                        && b.getUser().getFullname().toLowerCase().contains(q)))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void cancelBooking(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new RuntimeException("Booking not found"));
        com.cinema.booking.pattern.state.booking.BookingContext context = new com.cinema.booking.pattern.state.booking.BookingContext(booking);
        context.cancel();
        bookingRepository.save(booking);

        // Release reserved resources only when booking has never been settled successfully.
        if (!paymentRepository.existsByBookingAndStatus(booking, Payment.PaymentStatus.SUCCESS)) {
            // Inventory and promotion inventory logic removed
        }
    }

    @Override
    @Transactional
    public void refundBooking(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new RuntimeException("Booking not found"));
        com.cinema.booking.pattern.state.booking.BookingContext context = new com.cinema.booking.pattern.state.booking.BookingContext(booking);
        context.refund();
        bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public void printTickets(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new RuntimeException("Booking not found"));
        com.cinema.booking.pattern.state.booking.BookingContext context = new com.cinema.booking.pattern.state.booking.BookingContext(booking);
        context.printTickets();
        // Maybe update DB if printing changes a specific physical flag, for now state pattern enforces rules.
    }

    private BookingDTO mapToDTO(Booking booking) {
        List<Ticket> tickets = ticketRepository.findByBooking_BookingId(booking.getBookingId());
        List<FnBLine> fnbs = fnBLineRepository.findByBooking_BookingId(booking.getBookingId());

        return BookingDTO.builder()
                .bookingId(booking.getBookingId())
                .bookingCode(booking.getBookingCode())
                .userId(booking.getUser() != null ? booking.getUser().getUserId() : null)
                .customerName(booking.getUser() != null ? booking.getUser().getFullname() : null)
                .customerPhone(booking.getUser() != null ? booking.getUser().getPhone() : null)
                .promotionId(booking.getPromotion() != null ? booking.getPromotion().getId() : null)
                .status(booking.getStatus())
                .createdAt(booking.getCreatedAt())
                .ticketList(tickets.stream().map(t -> BookingDTO.TicketLineDTO.builder()
                        .ticketId(t.getTicketId())
                        .movieId(t.getMovie() != null ? t.getMovie().getMovieId() : null)
                        .showtimeId(t.getShowtime() != null ? t.getShowtime().getShowtimeId() : null)
                        .seatId(t.getSeat() != null ? t.getSeat().getSeatId() : null)
                        .seatCode(t.getSeat() != null ? t.getSeat().getSeatCode() : null)
                        .seatType(t.getSeat() != null && t.getSeat().getSeatType() != null ? t.getSeat().getSeatType().getName() : null)
                        .unitPrice(t.getUnitPrice())
                        .holdExpiresAt(t.getHoldExpiresAt())
                        .build()).toList())
                .fnbLines(fnbs.stream().map(l -> BookingDTO.FnBLineDTO.builder()
                        .id(l.getId())
                        .fnbItemId(l.getFnbItem() != null ? l.getFnbItem().getFnbItemId() : null)
                        .itemName(l.getFnbItem() != null ? l.getFnbItem().getName() : null)
                        .unitPrice(l.getFnbItem() != null ? l.getFnbItem().getPrice() : null)
                        .quantity(l.getQuantity())
                        .build()).toList())
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
