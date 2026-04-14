package com.cinema.booking.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "staffs")
@PrimaryKeyJoinColumn(name = "user_id", referencedColumnName = "id")
@Getter
@Setter
@NoArgsConstructor
public class Staff extends User {

    @Override
    public String getSpringSecurityRole() {
        return "STAFF";
    }
}
