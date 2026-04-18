package com.cinema.booking.dtos;

import lombok.Data;

/** Phản hồi tạo URL thanh toán VNPay. */
@Data
public class VnpayPaymentResponse {
    private String payUrl;
}
