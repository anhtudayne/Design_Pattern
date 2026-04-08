package com.cinema.booking.services;

public interface CheckoutService {
    String createBooking(Integer userId, Integer showtimeId, java.util.List<Integer> seatIds, java.util.List<com.cinema.booking.dtos.BookingCalculationDTO.FnbOrderDTO> fnbs, String promoCode) throws Exception;
    void processMomoCallback(com.cinema.booking.dtos.MomoCallbackRequest callback) throws Exception;
}
