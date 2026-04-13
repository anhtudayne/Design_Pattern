package com.cinema.booking.services;

import com.cinema.booking.dtos.BookingCalculationDTO;
import com.cinema.booking.dtos.PriceBreakdownDTO;

/**
 * Dynamic Pricing Engine — tính giá vé thông minh dựa trên nhiều yếu tố:
 * <ul>
 *   <li>Ngày lễ / cuối tuần (Strategy pattern)</li>
 *   <li>Đặt vé sớm — EarlyBird (Strategy pattern)</li>
 *   <li>Tỷ lệ lấp đầy (Decorator — OccupancyDecorator)</li>
 *   <li>Hạng thành viên (Decorator — MemberDiscountDecorator)</li>
 *   <li>Voucher / promotion (Decorator — VoucherDecorator)</li>
 * </ul>
 *
 * Kết quả được validate qua Chain of Responsibility trước khi trả về.
 *
 * <p>Cùng signature với {@code BookingService.calculatePrice()} để
 * {@code BookingServiceImpl} có thể delegate trực tiếp.</p>
 */
public interface DynamicPricingService {

    PriceBreakdownDTO calculatePrice(BookingCalculationDTO request);
}
