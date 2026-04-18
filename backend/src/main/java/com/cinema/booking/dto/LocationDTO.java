package com.cinema.booking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationDTO {
    /** Khớp entity {@code Location#id}; JSON dùng {@code locationId} cho frontend admin. */
    @JsonProperty("locationId")
    private Integer id;
    private String name;
}
