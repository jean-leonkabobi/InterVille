package com.transport.api.bus.entity;

import com.transport.api.common.BaseEntity;
import com.transport.api.bus.enums.TypeSiege;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "siege", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"bus_id", "seat_number"})
})
@Getter
@Setter
public class Siege extends BaseEntity {

    @Column(name = "bus_id", nullable = false)
    private Long busId;

    @Column(name = "seat_number", nullable = false, length = 10)
    private String seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_type", length = 20)
    private TypeSiege seatType = TypeSiege.STANDARD;

    @Column(name = "position_x")
    private Integer positionX;

    @Column(name = "position_y")
    private Integer positionY;
}