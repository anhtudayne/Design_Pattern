package com.cinema.booking.dtos;

import lombok.Data;

@Data
public class UserUpdateRequest {
    private String fullname;
    private String phone;
}
