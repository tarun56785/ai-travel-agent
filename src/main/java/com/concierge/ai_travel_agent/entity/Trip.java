package com.concierge.ai_travel_agent.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "saved_trips")
@Data
@NoArgsConstructor
public class Trip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String customerEmail;

    @Column(nullable = false)
    private String destination;

    @Column(length = 1000)
    private String flightDetails;

    private String status;
    private LocalDateTime createdAt;

    public Trip(String customerEmail, String destination, String flightDetails) {
        this.customerEmail = customerEmail;
        this.destination = destination;
        this.flightDetails = flightDetails;
        this.status = "FINALIZED";
        this.createdAt = LocalDateTime.now();
    }
}
