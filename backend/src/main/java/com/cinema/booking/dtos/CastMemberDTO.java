package com.cinema.booking.dtos;

import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CastMemberDTO {
    private Integer cast_memberID;
    private String full_name;
    private String bio;
    private LocalDate birth_date;
    private String nationality;
}
