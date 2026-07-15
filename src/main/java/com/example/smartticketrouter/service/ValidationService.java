package com.example.smartticketrouter.service;

import com.example.smartticketrouter.model.TicketResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ValidationService {

    private static final List<String> VALID_CATEGORIES =
            List.of("Billing", "Technical", "Account", "Orders", "Shipping", "Refund", "Product", "General");

    private static final List<String> VALID_PRIORITIES = List.of("High", "Medium", "Low");

    private static final Map<String, String> TEAM_MAP = Map.of(
            "Billing", "Billing Team",
            "Technical", "Technical Support",
            "Account", "Account Management",
            "Orders", "Order Management",
            "Shipping", "Logistics Team",
            "Refund", "Refund Department",
            "Product", "Product Support",
            "General", "Customer Care"
    );

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    public void validateRequestOrThrow(String title, String email, String createdBy) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (createdBy == null || createdBy.isBlank()) {
            throw new IllegalArgumentException("createdBy is required");
        }
        if (email == null || !email.matches(EMAIL_REGEX)) {
            throw new IllegalArgumentException("Invalid email address: " + email);
        }
    }

    // Throws if the AI's response doesn't meet our rules; does nothing if it's valid.
    public void validateOrThrow(TicketResponse r) {
        if (r == null
                || r.getCategory() == null
                || r.getPriority() == null
                || r.getAssignedTeam() == null
                || r.getReason() == null
                || r.getConfidence() == null) {
            throw new IllegalStateException("AI response is missing one or more required fields");
        }
        if (!VALID_CATEGORIES.contains(r.getCategory())) {
            throw new IllegalStateException("AI returned an unrecognized category: " + r.getCategory());
        }
        if (!VALID_PRIORITIES.contains(r.getPriority())) {
            throw new IllegalStateException("AI returned an unrecognized priority: " + r.getPriority());
        }
        if (!TEAM_MAP.get(r.getCategory()).equals(r.getAssignedTeam())) {
            throw new IllegalStateException("Assigned team does not match category mapping");
        }
        if (r.getConfidence() < 0.0 || r.getConfidence() > 1.0) {
            throw new IllegalStateException("Confidence is out of valid range");
        }
    }
}