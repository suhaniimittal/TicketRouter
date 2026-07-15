package com.example.smartticketrouter.model;

import lombok.Data;

@Data
public class TicketRequest {

    private String ticketId;
    private String title;
    private String message;
    private String createdBy;
    private String email;

}