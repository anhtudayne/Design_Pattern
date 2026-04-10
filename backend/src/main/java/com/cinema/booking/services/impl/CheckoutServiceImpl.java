package com.cinema.booking.services.impl;

import com.cinema.booking.dtos.BookingCalculationDTO;
import com.cinema.booking.dtos.MomoCallbackRequest;
import com.cinema.booking.dtos.MomoPaymentResponse;
import com.cinema.booking.dtos.PriceBreakdownDTO;
import com.cinema.booking.entities.*;
import com.cinema.booking.repositories.*;
import com.cinema.booking.services.BookingService;
import com.cinema.booking.services.CheckoutService;
import com.cinema.booking.services.EmailService;
import com.cinema.booking.services.MomoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class CheckoutServiceImpl implements CheckoutService {

    @Value("${momo.dev-all-success}")
    private boolean devAllSuccess;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ShowtimeRepository showtimeRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private MomoService momoService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private FnBLineRepository fnBLineRepository;

    @Autowired
    private FnbItemRepository fnbItemRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Override
    @Transactional
    public String createBooking(Integer userId, Integer showtimeId, List<Integer> seatIds, List<BookingCalculationDTO.FnbOrderDTO> fnbs, String promoCode) throws Exception {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        if (!(user instanceof Customer customer)) {
            throw new RuntimeException("Chỉ Customer mới có thể đặt vé.");
        }
        // Kiểm tra xem ghế đã được bán chưa
        for (Integer seatId : seatIds) {
            if (ticketRepository.existsByShowtime_ShowtimeIdAndSeat_SeatId(showtimeId, seatId)) {
                throw new RuntimeException("Ghế ID " + seatId + " đã được bán. Vui lòng chọn ghế khác.");
            }
        }

        Promotion promotion = promoCode != null ? promotionRepository.findByCode(promoCode).orElse(null) : null;

        // Tính giá final
        BookingCalculationDTO calculationRequest = new BookingCalculationDTO();
        calculationRequest.setShowtimeId(showtimeId);
        calculationRequest.setSeatIds(seatIds);
        calculationRequest.setFnbs(fnbs);
        calculationRequest.setPromoCode(promoCode);
        PriceBreakdownDTO price = bookingService.calculatePrice(calculationRequest);

        // Lưu Booking PENDING
        Booking booking = Booking.builder()
                .customer(customer)
                .promotion(promotion)
                .status(Booking.BookingStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        booking = bookingRepository.save(booking);

        // Lưu F&B
        if (fnbs != null && !fnbs.isEmpty()) {
            for (BookingCalculationDTO.FnbOrderDTO fnbDto : fnbs) {
                FnbItem fnbItem = fnbItemRepository.findById(fnbDto.getItemId())
                        .orElseThrow(() -> new RuntimeException("Sản phẩm F&B không tồn tại: " + fnbDto.getItemId()));
                
                FnBLine item = FnBLine.builder()
                        .booking(booking)
                        .item(fnbItem)
                        .quantity(fnbDto.getQuantity())
                        .unitPrice(fnbItem.getPrice())
                        .build();
                fnBLineRepository.save(item);
            }
        }

        // Tạo MoMo Payment URL
        // Gửi kèm seatIds qua extraData để khi callback quay lại có thông tin tạo Ticket
        String seatIdsStr = (seatIds != null) ? seatIds.stream().map(String::valueOf).collect(Collectors.joining(",")) : "";
        String extraData = booking.getBookingId() + "|" + showtimeId + "|" + seatIdsStr;

        MomoPaymentResponse momoResponse = momoService.createPayment(
                "BOOKING_" + booking.getBookingId(),
                price.getFinalTotal().longValue(),
                "Thanh toán vé xem phim StarCine cho Booking #" + booking.getBookingId(),
                extraData
        );
        // Tạo Payment record PENDING
        try {
            Payment payment = Payment.builder()
                    .booking(booking)
                    .paymentMethod("MOMO")
                    .amount(price.getFinalTotal())
                    .status(Payment.PaymentStatus.PENDING)
                    .build();
            paymentRepository.save(payment);
        } catch (Exception e) {
            System.err.println(">>> [StarCine] ERROR khi lưu Payment PENDING: " + e.getMessage());
        }

        return momoResponse.getPayUrl();
    }

    @Override
    @Transactional
    public void processMomoCallback(MomoCallbackRequest callback) throws Exception {
        if (!momoService.verifySignature(callback)) {
            throw new RuntimeException("Chữ ký MoMo không hợp lệ");
        }

        if (callback.getExtraData() == null || callback.getExtraData().isEmpty()) {
            throw new RuntimeException("Thiếu extraData (bookingId) từ MoMo callback");
        }

        String extraData = callback.getExtraData();
        String[] parts = extraData.split("\\|");
        Integer bookingId = Integer.parseInt(parts[0]);
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new RuntimeException("Không tìm thấy Booking ID: " + bookingId));

        // Nếu chế độ "Tất cả thành công" đang bật HOẶC resultCode == 0 (Thanh toán thành công thực sự)
        if (devAllSuccess || callback.getResultCode() == 0) {
            System.out.println(">>> [StarCine] Xử lý thanh toán THÀNH CÔNG cho Booking #" + bookingId);
            
            // 1. Thanh toán THÀNH CÔNG -> Cập nhật trạng thái Booking
            booking.confirm();
            bookingRepository.save(booking);

            // 1.5 Cập nhật tổng chi tiêu cho khách hàng (Customer)
            Customer customer = booking.getCustomer();
            if (customer != null) {
                BigDecimal paidAmount = new BigDecimal(callback.getAmount());
                safeIncreaseCustomerSpending(customer.getUserId(), paidAmount);
                System.out.println(">>> [StarCine] Đã cộng total_spending cho User #" + customer.getUserId() + " thêm: " + paidAmount);
            }

            // 1.7 Bóc tách seatIds từ extraData và tạo Ticket thực sự cho Booking này
            // Log thực tế extraData để debug chính xác
            System.out.println(">>> [StarCine] rawExtraData nhận được: " + extraData);
            
            // Thử Decode nếu extraData bị URL encode (vd: %7C cho |)
            if (extraData != null && extraData.contains("%7C")) {
                try {
                    extraData = java.net.URLDecoder.decode(extraData, "UTF-8");
                    System.out.println(">>> [StarCine] Decoded extraData thành công: " + extraData);
                    parts = extraData.split("\\|");
                } catch (java.io.UnsupportedEncodingException e) {
                    System.err.println(">>> [StarCine] ERROR khi decode extraData: " + e.getMessage());
                }
            }

            if (parts.length > 2 && !parts[2].isEmpty()) {
                Integer showtimeId = Integer.parseInt(parts[1]);
                String[] seatIdStrs = parts[2].split(",");
                Showtime showtime = showtimeRepository.findById(showtimeId)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy Showtime ID: " + showtimeId));
                int ticketsCreated = 0;
                
                for (String sId : seatIdStrs) {
                    try {
                        Integer seatId = Integer.parseInt(sId.trim());
                        Seat seat = seatRepository.findById(seatId).orElse(null);
                        if (seat != null) {
                            BigDecimal ticketPrice = showtime.getBasePrice()
                                    .add(seat.getSeatType() != null && seat.getSeatType().getPriceSurcharge() != null
                                            ? seat.getSeatType().getPriceSurcharge()
                                            : BigDecimal.ZERO);

                            Ticket ticket = Ticket.builder()
                                    .booking(booking)
                                    .seat(seat)
                                    .showtime(showtime)
                                    .price(ticketPrice)
                                    .build();
                            ticketRepository.save(ticket);
                            ticketsCreated++;
                        } else {
                            System.err.println(">>> [StarCine] WARNING: Không tìm thấy Ghế (Seat) với ID: " + seatId);
                        }
                    } catch (NumberFormatException nfe) {
                        System.err.println(">>> [StarCine] ERROR: Không thể parse seatId từ chuỗi: " + sId);
                    }
                }
                System.out.println(">>> [StarCine] Đã tạo THÀNH CÔNG " + ticketsCreated + " vé cho Booking #" + bookingId);
            } else {
                System.err.println(">>> [StarCine] BỎ QUA TẠO VÉ: extraData không đúng định dạng mong muốn 'id|seatids' hoặc seatids bị trống.");
            }

            // 2. Cập nhật record Payment hiện tại từ PENDING -> SUCCESS
            try {
                // Lấy ra Payment PENDING mới nhất của Booking này để cập nhật
                List<Payment> payments = paymentRepository.findByBookingAndPaymentMethodAndStatus(booking, "MOMO", Payment.PaymentStatus.PENDING);
                
                Payment payment;
                if (!payments.isEmpty()) {
                    payment = payments.get(payments.size() - 1); // Lấy cái cuối cùng (mới nhất)
                } else {
                    // Nếu không thấy (đề phòng), tạo mới 
                    payment = Payment.builder()
                            .booking(booking)
                            .paymentMethod("MOMO")
                            .amount(new BigDecimal(callback.getAmount()))
                            .build();
                }

                payment.setStatus(Payment.PaymentStatus.SUCCESS);
                payment.setPaidAt(LocalDateTime.now());
                paymentRepository.save(payment);
            } catch (Exception e) {
                System.err.println(">>> [StarCine] ERROR khi cập nhật Payment: " + e.getMessage());
                // Không throw exception để tránh rollback trạng thái PAID của Booking
            }

            // 3. Gửi Email vé
            try {
                System.out.println(">>> [StarCine] Đang gửi Email vé trực tiếp...");
                emailService.sendTicketEmail(bookingId);
            } catch (Exception e) {
                System.err.println(">>> [StarCine] ERROR khi gửi Email: " + e.getMessage());
            }
        } else {
            System.out.println(">>> [StarCine] Xử lý thanh toán THẤT BẠI cho Booking #" + bookingId + " (resultCode=" + callback.getResultCode() + ")");
            // Cập nhật record Payment sang FAILED
            try {
                List<Payment> payments = paymentRepository.findByBookingAndPaymentMethodAndStatus(booking, "MOMO", Payment.PaymentStatus.PENDING);
                
                if (!payments.isEmpty()) {
                    Payment payment = payments.get(payments.size() - 1);
                    payment.setStatus(Payment.PaymentStatus.FAILED);
                    paymentRepository.save(payment);
                }
            } catch (Exception e) {
                System.err.println(">>> [StarCine] ERROR khi cập nhật Payment FAILED: " + e.getMessage());
            }

            booking.setStatus(Booking.BookingStatus.CANCELLED);
            bookingRepository.save(booking);
        }
    }

    @Override
    @Transactional
    public java.util.Map<String, Object> processDemoCheckout(
            Integer userId,
            Integer showtimeId,
            List<Integer> seatIds,
            List<BookingCalculationDTO.FnbOrderDTO> fnbs,
            String promoCode,
            boolean success
    ) throws Exception {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        if (!(user instanceof Customer customer)) {
            throw new RuntimeException("Chỉ Customer mới có thể đặt vé.");
        }
        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new RuntimeException("Suất chiếu không tồn tại"));

        for (Integer seatId : seatIds) {
            if (ticketRepository.existsByShowtime_ShowtimeIdAndSeat_SeatId(showtimeId, seatId)) {
                throw new RuntimeException("Ghế ID " + seatId + " đã được bán. Vui lòng chọn ghế khác.");
            }
        }

        Promotion promotion = promoCode != null ? promotionRepository.findByCode(promoCode).orElse(null) : null;

        BookingCalculationDTO calculationRequest = new BookingCalculationDTO();
        calculationRequest.setShowtimeId(showtimeId);
        calculationRequest.setSeatIds(seatIds);
        calculationRequest.setFnbs(fnbs);
        calculationRequest.setPromoCode(promoCode);
        PriceBreakdownDTO price = bookingService.calculatePrice(calculationRequest);

        Booking booking = Booking.builder()
                .customer(customer)
                .promotion(promotion)
                .status(success ? Booking.BookingStatus.CONFIRMED : Booking.BookingStatus.CANCELLED)
                .createdAt(LocalDateTime.now())
                .build();
        booking = bookingRepository.save(booking);

        if (fnbs != null && !fnbs.isEmpty()) {
            for (BookingCalculationDTO.FnbOrderDTO fnbDto : fnbs) {
                FnbItem fnbItem = fnbItemRepository.findById(fnbDto.getItemId())
                        .orElseThrow(() -> new RuntimeException("Sản phẩm F&B không tồn tại: " + fnbDto.getItemId()));
                FnBLine item = FnBLine.builder()
                        .booking(booking)
                        .item(fnbItem)
                        .quantity(fnbDto.getQuantity())
                        .unitPrice(fnbItem.getPrice())
                        .build();
                fnBLineRepository.save(item);
            }
        }

        if (success) {
            for (Integer seatId : seatIds) {
                Seat seat = seatRepository.findById(seatId)
                        .orElseThrow(() -> new RuntimeException("Ghế không tồn tại: " + seatId));
                BigDecimal ticketPrice = showtime.getBasePrice()
                        .add(seat.getSeatType() != null && seat.getSeatType().getPriceSurcharge() != null
                                ? seat.getSeatType().getPriceSurcharge()
                                : BigDecimal.ZERO);
                Ticket ticket = Ticket.builder()
                        .booking(booking)
                        .seat(seat)
                        .showtime(showtime)
                        .price(ticketPrice)
                        .build();
                ticketRepository.save(ticket);
            }

            safeIncreaseCustomerSpending(customer.getUserId(), price.getFinalTotal());
        }

        Payment payment = Payment.builder()
                .booking(booking)
                .paymentMethod("MOMO")
                .amount(price.getFinalTotal())
                .status(success ? Payment.PaymentStatus.SUCCESS : Payment.PaymentStatus.FAILED)
                .paidAt(success ? LocalDateTime.now() : null)
                .build();
        paymentRepository.save(payment);

        if (success) {
            try {
                emailService.sendTicketEmail(booking.getBookingId());
            } catch (Exception e) {
                System.err.println(">>> [StarCine] ERROR gửi email demo checkout: " + e.getMessage());
            }
        }

        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("bookingId", booking.getBookingId());
        result.put("bookingCode", booking.getBookingCode());
        result.put("paymentStatus", payment.getStatus().name());
        result.put("totalAmount", price.getFinalTotal());
        return result;
    }

    private void safeIncreaseCustomerSpending(Integer userId, BigDecimal amount) {
        RuntimeException last = null;
        for (int i = 0; i < 3; i++) {
            try {
                customerRepository.increaseTotalSpending(userId, amount);
                return;
            } catch (RuntimeException ex) {
                last = ex;
                String msg = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";
                if (!msg.contains("deadlock")) {
                    throw ex;
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(120L * (i + 1));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw ex;
                }
            }
        }
        throw last != null ? last : new RuntimeException("Không thể cập nhật tổng chi tiêu khách hàng");
    }
}
