package com.cinema.booking.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "staffs")
@PrimaryKeyJoinColumn(name = "user_ID")
@Getter
@Setter
@NoArgsConstructor
public class Staff extends User {
}
