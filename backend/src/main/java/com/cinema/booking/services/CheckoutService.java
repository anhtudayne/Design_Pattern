package com.cinema.booking.services;

public interface CheckoutService {
    /**
     * @param paymentMethod mã kênh thanh toán (vd. MOMO); null hoặc rỗng = MOMO. Luồng DEMO dùng {@link #processDemoCheckout}.
     */
    String createBooking(Integer userId, Integer showtimeId, java.util.List<Integer> seatIds, java.util.List<com.cinema.booking.dtos.BookingCalculationDTO.FnbOrderDTO> fnbs, String promoCode, String paymentMethod) throws Exception;
    void processMomoCallback(com.cinema.booking.dtos.MomoCallbackRequest callback) throws Exception;
    java.util.Map<String, Object> processDemoCheckout(Integer userId, Integer showtimeId, java.util.List<Integer> seatIds, java.util.List<com.cinema.booking.dtos.BookingCalculationDTO.FnbOrderDTO> fnbs, String promoCode, boolean success) throws Exception;
    java.util.Map<String, Object> processStaffCashCheckout(Integer customerId, Integer showtimeId, java.util.List<Integer> seatIds, java.util.List<com.cinema.booking.dtos.BookingCalculationDTO.FnbOrderDTO> fnbs, String promoCode) throws Exception;
}
