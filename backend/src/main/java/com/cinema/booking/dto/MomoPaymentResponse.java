package com.cinema.booking.dto;

import lombok.Data;

/** Phản hồi tạo thanh toán MoMo — URL redirect khách sang ví. */
@Data
public class MomoPaymentResponse {
    private String payUrl;
    private String deeplink;
    private String qrCodeUrl;
}
