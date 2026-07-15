package com.example.smartticketrouter.model;

import lombok.Data;

@Data
public class TicketResponse {

    private String category;

    private String priority;

    private String assignedTeam;

    private String reason;

    private Double confidence;
}
