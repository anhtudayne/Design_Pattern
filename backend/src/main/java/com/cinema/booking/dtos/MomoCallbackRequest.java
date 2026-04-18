package com.cinema.booking.dtos;

import lombok.Data;

/** Tham số redirect / IPN từ MoMo (snake_case JSON → camelCase qua Jackson tuỳ cấu hình). */
@Data
public class MomoCallbackRequest {
    private String partnerCode;
    private String orderId;
    private String requestId;
    private String amount;
    private String orderInfo;
    private String orderType;
    private String transId;
    private Integer resultCode;
    private String message;
    private String payType;
    private String responseTime;
    private String extraData;
    private String signature;
}
