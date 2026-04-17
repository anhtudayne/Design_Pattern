package com.cinema.booking.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "customers")
@PrimaryKeyJoinColumn(name = "user_ID")
@Getter
@Setter
@NoArgsConstructor
public class Customer extends User {
}
