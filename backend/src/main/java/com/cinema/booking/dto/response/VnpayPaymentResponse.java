package com.cinema.booking.dto.response;

import lombok.Data;

/** Phản hồi tạo URL thanh toán VNPay. */
@Data
public class VnpayPaymentResponse {
    private String payUrl;
}
