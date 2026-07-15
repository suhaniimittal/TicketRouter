package com.example.smartticketrouter.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="tickets")
public class TicketEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique= true,nullable = false)
    private String ticketId;

    private String title;

    private String createdBy;

    private String email;

    @Column(length = 1000)
    private String message;

    @Column(length = 1000)
    private String normalizedMessage;

    private String category;

    private String priority;

    private String assignedTeam;

    @Column(length = 1000)
    private String reason;

    private Double confidence;

    private Long processingTimeMs;   // NEW: how long the AI classification took

    private Boolean needsReview;

    private LocalDateTime createdAt;
}

