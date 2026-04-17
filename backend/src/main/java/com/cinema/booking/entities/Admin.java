package com.cinema.booking.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "admins")
@PrimaryKeyJoinColumn(name = "user_ID")
@Getter
@Setter
@NoArgsConstructor
public class Admin extends User {
}
