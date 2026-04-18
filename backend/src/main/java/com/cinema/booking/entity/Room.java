package com.cinema.booking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "rooms")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer roomId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cinema_id", nullable = false)
    private Cinema cinema;

    @Column(nullable = false, length = 50)
    private String name;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Seat> seatList;

    @Column(name = "screen_type")
    private String screenType; // 2D, 3D, IMAX

    /**
     * Trả về sức chứa phòng (số ghế).
     */
    public int getCapacity() {
        return seatList != null ? seatList.size() : 0;
    }
}
