package com.cinema.booking.services.impl;

import com.cinema.booking.dtos.BookingCalculationDTO;
import com.cinema.booking.dtos.CheckoutRequest;
import com.cinema.booking.dtos.CheckoutResult;
import com.cinema.booking.dtos.MomoCallbackRequest;
import com.cinema.booking.dtos.PriceBreakdownDTO;
import com.cinema.booking.entities.*;
import com.cinema.booking.repositories.*;
import com.cinema.booking.services.BookingService;
import com.cinema.booking.services.CheckoutService;
import com.cinema.booking.services.EmailService;
import com.cinema.booking.services.MomoService;
import com.cinema.booking.services.factory.BookingFactory;
import com.cinema.booking.services.template_method.checkout.DemoCheckoutProcess;
import com.cinema.booking.services.template_method.checkout.MomoCheckoutProcess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

    @Autowired
    private BookingFactory bookingFactory;

    // ──────────────────────────────────────────────────────────────────
    //  Template Method Pattern: Inject 2 concrete checkout processes
    // ──────────────────────────────────────────────────────────────────
    @Autowired
    private MomoCheckoutProcess momoCheckoutProcess;

    @Autowired
    private DemoCheckoutProcess demoCheckoutProcess;

    // ═══════════════════════════════════════════════════════════════════
    //  1. createBooking — delegate to MomoCheckoutProcess (Template Method)
    // ═══════════════════════════════════════════════════════════════════
    @Override
    @Transactional
    public String createBooking(Integer userId, Integer showtimeId, List<Integer> seatIds,
                                List<BookingCalculationDTO.FnbOrderDTO> fnbs, String promoCode) throws Exception {
        CheckoutRequest request = CheckoutRequest.builder()
                .userId(userId)
                .showtimeId(showtimeId)
                .seatIds(seatIds)
                .fnbs(fnbs)
                .promoCode(promoCode)
                .build();

        CheckoutResult result = momoCheckoutProcess.checkout(request);
        // processPayment() returns the MoMo payUrl string
        return (String) result.getPaymentResult();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  2. processMomoCallback — giữ nguyên logic callback (không refactor)
    //     Vì callback không phải luồng "checkout mới" mà là xử lý kết quả
    //     từ MoMo trả về, cấu trúc khác hoàn toàn so với checkout flow.
    // ═══════════════════════════════════════════════════════════════════
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

                            // Sử dụng BookingFactory (Factory Method Pattern)
                            Ticket ticket = bookingFactory.createTicket(booking, seat, showtime, ticketPrice);
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
                List<Payment> payments = paymentRepository.findByBookingAndPaymentMethodAndStatus(booking, "MOMO", Payment.PaymentStatus.PENDING);

                Payment payment;
                if (!payments.isEmpty()) {
                    payment = payments.get(payments.size() - 1); // Lấy cái cuối cùng (mới nhất)
                } else {
                    // Nếu không thấy (đề phòng), tạo mới bằng Factory Method
                    payment = bookingFactory.createPayment(booking, "MOMO", new BigDecimal(callback.getAmount()), Payment.PaymentStatus.SUCCESS);
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

    // ═══════════════════════════════════════════════════════════════════
    //  3. processDemoCheckout — delegate to DemoCheckoutProcess (Template Method)
    // ═══════════════════════════════════════════════════════════════════
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
        CheckoutRequest request = CheckoutRequest.builder()
                .userId(userId)
                .showtimeId(showtimeId)
                .seatIds(seatIds)
                .fnbs(fnbs)
                .promoCode(promoCode)
                .demoSuccess(success)
                .build();

        CheckoutResult result = demoCheckoutProcess.checkout(request);

        // Build response Map từ CheckoutResult
        return demoCheckoutProcess.buildDemoResult(
                result.getBooking(),
                (Payment) result.getPaymentResult(),
                result.getPrice()
        );
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
