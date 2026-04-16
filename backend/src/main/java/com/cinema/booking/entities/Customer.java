package com.cinema.booking.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "customers")
@PrimaryKeyJoinColumn(name = "user_id", referencedColumnName = "id")
@Getter
@Setter
@NoArgsConstructor
public class Customer extends User {

    @Override
    public String getSpringSecurityRole() {
        return "USER";
    }

    public void bookTicket() {
        // Stub
    }

    public void writeReview() {
        // Stub
    }
}
