package com.example.smartticketrouter.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TicketRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String message;

    @NotBlank(message = "createdBy is required")
    private String createdBy;

    @NotBlank(message = "Email is required")
    //@Email(message = "Email must be a valid email address")
    private String email;
}