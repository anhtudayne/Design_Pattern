package com.cinema.booking.services.impl;

import com.cinema.booking.dtos.BookingCalculationDTO;
import com.cinema.booking.dtos.PriceBreakdownDTO;
import com.cinema.booking.dtos.SeatStatusDTO;
import com.cinema.booking.entities.*;
import com.cinema.booking.repositories.*;
import com.cinema.booking.services.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    @Autowired
    private FnbItemRepository fnbItemRepository;

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private com.cinema.booking.services.VoucherService voucherService;

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
        Set<Integer> soldSeatIds = ticketRepository.findByBooking_Showtime_ShowtimeId(showtimeId).stream()
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
                    .add(showtime.getSurcharge() != null ? showtime.getSurcharge() : BigDecimal.ZERO)
                    .add(seat.getPriceSurcharge() != null ? seat.getPriceSurcharge() : BigDecimal.ZERO);

            return SeatStatusDTO.builder()
                    .seatId(seat.getSeatId())
                    .seatRow(seat.getSeatRow())
                    .seatNumber(seat.getSeatNumber())
                    .seatType(seat.getSeatType().name())
                    .totalPrice(totalPrice)
                    .status(status)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public boolean lockSeat(Integer showtimeId, Integer seatId, Integer userId) {
        String key = getLockKey(showtimeId, seatId);
        // Kiểm tra xem ghế đã bán chưa
        boolean isSold = ticketRepository.findByBooking_Showtime_ShowtimeId(showtimeId).stream()
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
        Showtime showtime = showtimeRepository.findById(request.getShowtimeId())
                .orElseThrow(() -> new RuntimeException("Suất chiếu không tồn tại!"));

        // Tính tiền vé
        BigDecimal ticketTotal = BigDecimal.ZERO;
        List<Seat> seats = seatRepository.findAllById(request.getSeatIds());
        for (Seat seat : seats) {
            BigDecimal price = showtime.getBasePrice()
                    .add(showtime.getSurcharge() != null ? showtime.getSurcharge() : BigDecimal.ZERO)
                    .add(seat.getPriceSurcharge() != null ? seat.getPriceSurcharge() : BigDecimal.ZERO);
            ticketTotal = ticketTotal.add(price);
        }

        // Tính tiền F&B
        BigDecimal fnbTotal = BigDecimal.ZERO;
        if (request.getFnbs() != null) {
            for (BookingCalculationDTO.FnbOrderDTO fnbOrder : request.getFnbs()) {
                FnbItem item = fnbItemRepository.findById(fnbOrder.getItemId())
                        .orElseThrow(() -> new RuntimeException("Sản phẩm F&B không tồn tại!"));
                fnbTotal = fnbTotal.add(item.getPrice().multiply(new BigDecimal(fnbOrder.getQuantity())));
            }
        }

        BigDecimal subtotal = ticketTotal.add(fnbTotal);
        BigDecimal discountAmount = BigDecimal.ZERO;

        // Áp dụng Voucher
        if (request.getPromoCode() != null && !request.getPromoCode().isEmpty()) {
            Promotion promo = promotionRepository.findByCode(request.getPromoCode())
                    .orElse(null);
            
            if (promo != null) {
                if (LocalDateTime.now().isAfter(promo.getValidFrom()) 
                        && LocalDateTime.now().isBefore(promo.getValidTo())
                        && subtotal.compareTo(promo.getMinPurchaseAmount() != null ? promo.getMinPurchaseAmount() : BigDecimal.ZERO) >= 0) {
                    
                    discountAmount = subtotal.multiply(promo.getDiscountPercentage().divide(new BigDecimal("100")));
                    if (promo.getMaxDiscountAmount() != null && discountAmount.compareTo(promo.getMaxDiscountAmount()) > 0) {
                        discountAmount = promo.getMaxDiscountAmount();
                    }
                }
            } else {
                // Nếu không thấy trong DB, tìm trong Redis
                com.cinema.booking.dtos.VoucherDTO redisVoucher = voucherService.getVoucher(request.getPromoCode()).orElse(null);
                if (redisVoucher != null) {
                    BigDecimal minAmount = redisVoucher.getMinPurchaseAmount() != null ? redisVoucher.getMinPurchaseAmount() : BigDecimal.ZERO;
                    if (subtotal.compareTo(minAmount) >= 0) {
                        discountAmount = subtotal.multiply(redisVoucher.getDiscountPercentage().divide(new BigDecimal("100")));
                        
                        BigDecimal maxDAmount = redisVoucher.getMaxDiscountAmount();
                        if (maxDAmount != null && discountAmount.compareTo(maxDAmount) > 0) {
                            discountAmount = maxDAmount;
                        }
                    }
                }
            }
        }

        return PriceBreakdownDTO.builder()
                .ticketTotal(ticketTotal)
                .fnbTotal(fnbTotal)
                .discountAmount(discountAmount)
                .finalTotal(subtotal.subtract(discountAmount))
                .build();
    }
}
