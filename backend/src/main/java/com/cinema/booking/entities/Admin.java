package com.cinema.booking.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "admins")
@PrimaryKeyJoinColumn(name = "user_id", referencedColumnName = "id")
@Getter
@Setter
@NoArgsConstructor
public class Admin extends User {

    @Override
    public String getSpringSecurityRole() {
        return "ADMIN";
    }

    public void manageUsers() {
        // Stub
    }

    public void manageCinemas() {
        // Stub
    }

    public void viewSystemReports() {
        // Stub
    }

    public void viewDashboard() {
        // Stub
    }
}
