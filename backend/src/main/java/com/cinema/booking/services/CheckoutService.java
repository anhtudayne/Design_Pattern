package com.cinema.booking.services;

public interface CheckoutService {

    /**
     * MOMO / VNPAY cổng online — trả payUrl. Tiền mặt dùng {@link #processCustomerCashCheckout} / staff cash.
     */
    String createBooking(
            Integer userId,
            Integer showtimeId,
            java.util.List<Integer> seatIds,
            java.util.List<com.cinema.booking.dtos.BookingCalculationDTO.FnbOrderDTO> fnbs,
            String promoCode,
            String paymentMethod) throws Exception;

    void processMomoCallback(com.cinema.booking.dtos.MomoCallbackRequest callback) throws Exception;

    java.util.Map<String, Object> processStaffCashCheckout(
            Integer customerId,
            Integer showtimeId,
            java.util.List<Integer> seatIds,
            java.util.List<com.cinema.booking.dtos.BookingCalculationDTO.FnbOrderDTO> fnbs,
            String promoCode) throws Exception;

    java.util.Map<String, Object> processCustomerCashCheckout(
            Integer userId,
            Integer showtimeId,
            java.util.List<Integer> seatIds,
            java.util.List<com.cinema.booking.dtos.BookingCalculationDTO.FnbOrderDTO> fnbs,
            String promoCode) throws Exception;

    /** MoMo QR mô phỏng — {@link com.cinema.booking.services.template_method.checkout.LocalMomoCheckoutProcess}. */
    java.util.Map<String, Object> processMomoUiFinish(
            boolean success,
            Integer userId,
            Integer showtimeId,
            java.util.List<Integer> seatIds,
            java.util.List<com.cinema.booking.dtos.BookingCalculationDTO.FnbOrderDTO> fnbs,
            String promoCode) throws Exception;

    /** VNPay QR mô phỏng — {@link com.cinema.booking.services.template_method.checkout.LocalVnpayCheckoutProcess}. */
    java.util.Map<String, Object> processVnpayUiFinish(
            boolean success,
            Integer userId,
            Integer showtimeId,
            java.util.List<Integer> seatIds,
            java.util.List<com.cinema.booking.dtos.BookingCalculationDTO.FnbOrderDTO> fnbs,
            String promoCode) throws Exception;
}
