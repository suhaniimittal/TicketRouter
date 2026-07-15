package com.example.smartticketrouter.service;

import com.example.smartticketrouter.ai.TicketAssistant;
import com.example.smartticketrouter.entity.TicketEntity;
import com.example.smartticketrouter.model.TicketRequest;
import com.example.smartticketrouter.model.TicketResponse;
import com.example.smartticketrouter.repository.TicketRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class TicketService {

    private final TicketAssistant ticketAssistant;
    private final TicketRepository ticketRepository;

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

    private static final double LOW_CONFIDENCE_THRESHOLD = 0.5;

    public TicketService(TicketAssistant ticketAssistant, TicketRepository ticketRepository) {
        this.ticketAssistant = ticketAssistant;
        this.ticketRepository = ticketRepository;
    }

    public TicketEntity getTicketById(String ticketId) {
        return ticketRepository.findByTicketId(ticketId)
                .orElseThrow(() -> new NoSuchElementException("No ticket found with ID: " + ticketId));
    }

    public List<TicketEntity> getTicketsByPriority(String priority) {
        return ticketRepository.findByPriority(priority);
    }

    public List<TicketEntity> getTicketsByTeam(String assignedTeam) {
        return ticketRepository.findByAssignedTeam(assignedTeam);
    }

    public TicketResponse routeTicket(TicketRequest request) {

        if (request.getTicketId() == null || request.getTicketId().isBlank()) {
            request.setTicketId("TCK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }

        boolean isBlank = request.getMessage() == null || request.getMessage().isBlank();

        // NEW: compute the normalized version once, reuse it for both searching and saving
        String normalizedMessage = isBlank ? "" : normalize(request.getMessage());

        TicketResponse response;
        boolean needsReview;
        long startTime = System.currentTimeMillis();

        Optional<TicketEntity> existing = isBlank
                ? Optional.empty()
                : ticketRepository.findFirstByNormalizedMessageOrderByIdAsc(normalizedMessage);

        if (existing.isPresent()) {
            TicketEntity match = existing.get();
            response = new TicketResponse();
            response.setCategory(match.getCategory());
            response.setPriority(match.getPriority());
            response.setAssignedTeam(match.getAssignedTeam());
            response.setReason(match.getReason() + " (reused from ticket " + match.getTicketId() + ")");
            response.setConfidence(match.getConfidence());
            needsReview = Boolean.TRUE.equals(match.getNeedsReview());

        } else if (isBlank) {
            response = insufficientInfoResponse();
            needsReview = true;

        } else {
            TicketResponse attempt = null;
            boolean success = false;

            for (int attemptNumber = 1; attemptNumber <= 2 && !success; attemptNumber++) {
                try {
                    attempt = ticketAssistant.classify(request.getMessage());
                    validateOrThrow(attempt);
                    success = true;
                } catch (Exception e) {
                    System.out.println("Attempt " + attemptNumber + " failed: " + e.getMessage());
                    success = false;
                }
            }

            if (success) {
                response = attempt;
                needsReview = response.getConfidence() != null
                        && response.getConfidence() < LOW_CONFIDENCE_THRESHOLD;
            } else {
                response = fallbackResponse();
                needsReview = true;
            }
        }

        long processingTime = System.currentTimeMillis() - startTime;

        TicketEntity entity = new TicketEntity();
        entity.setTicketId(request.getTicketId());
        entity.setTitle(request.getTitle());
        entity.setCreatedBy(request.getCreatedBy());
        entity.setEmail(request.getEmail());
        entity.setMessage(request.getMessage());
        entity.setNormalizedMessage(normalizedMessage);   // NEW: save it for future duplicate lookups
        entity.setCategory(response.getCategory());
        entity.setPriority(response.getPriority());
        entity.setAssignedTeam(response.getAssignedTeam());
        entity.setReason(response.getReason());
        entity.setConfidence(response.getConfidence());
        entity.setProcessingTimeMs(processingTime);
        entity.setNeedsReview(needsReview);
        entity.setCreatedAt(LocalDateTime.now());

        try {
            ticketRepository.save(entity);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new IllegalArgumentException(
                    "A ticket with ID '" + request.getTicketId() + "' already exists.");
        }

        return response;
    }

    public List<TicketResponse> routeTicketsBatch(List<TicketRequest> requests) {
        return requests.stream()
                .map(this::routeTicket)
                .toList();
    }

    public List<TicketEntity> getAllTickets() {
        return ticketRepository.findAll();
    }

    // NEW: turns messy real-world text into a consistent comparable form
    private String normalize(String message) {
        return message
                .trim()                          // remove leading/trailing spaces
                .toLowerCase()                   // "Payment Failed" -> "payment failed"
                .replaceAll("\\s+", " ")          // collapse multiple spaces/tabs/newlines into one space
                .replaceAll("[!?.,]+$", "");      // strip trailing punctuation like "!!!" or "."
    }

    private void validateOrThrow(TicketResponse r) {
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

    private TicketResponse fallbackResponse() {
        TicketResponse r = new TicketResponse();
        r.setCategory("General");
        r.setPriority("Medium");
        r.setAssignedTeam("Customer Care");
        r.setReason("Automatic classification failed; routed for manual review.");
        r.setConfidence(0.0);
        return r;
    }

    private TicketResponse insufficientInfoResponse() {
        TicketResponse r = new TicketResponse();
        r.setCategory("General");
        r.setPriority("Low");
        r.setAssignedTeam("Customer Care");
        r.setReason("No message content provided.");
        r.setConfidence(0.0);
        return r;
    }
}