package com.cinema.booking.dto;

import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CastMemberDTO {
    private Integer castMemberId;
    private String fullName;
    private String bio;
    private LocalDate birthDate;
    private String nationality;
    private String imageUrl;
}
