package com.example.smartticketrouter.service;

import com.example.smartticketrouter.ai.TicketAssistant;
import com.example.smartticketrouter.entity.TicketEntity;
import com.example.smartticketrouter.model.TicketRequest;
import com.example.smartticketrouter.model.TicketResponse;
import com.example.smartticketrouter.repository.TicketRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TicketService {

    private final TicketAssistant ticketAssistant;
    private final TicketRepository ticketRepository;
    private final ValidationService validationService;
    private final NormalizationService normalizationService;
    private final FallbackService fallbackService;
    private final DuplicateCheckService duplicateCheckService;

    private static final double LOW_CONFIDENCE_THRESHOLD = 0.5;

    public TicketService(TicketAssistant ticketAssistant,
                         TicketRepository ticketRepository,
                         ValidationService validationService,
                         NormalizationService normalizationService,
                         FallbackService fallbackService,
                         DuplicateCheckService duplicateCheckService) {
        this.ticketAssistant = ticketAssistant;
        this.ticketRepository = ticketRepository;
        this.validationService = validationService;
        this.normalizationService = normalizationService;
        this.fallbackService = fallbackService;
        this.duplicateCheckService = duplicateCheckService;
    }

    public ResponseEntity<?> routeTicket(TicketRequest request) {

        // NEW: validate the customer's own input first (email format etc.)

        try {
            validationService.validateRequestOrThrow(
                    request.getTitle(),
                    request.getEmail(),
                    request.getCreatedBy()
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", e.getMessage())
            );
        }

        boolean isBlank = request.getMessage() == null || request.getMessage().isBlank();
        String normalizedMessage = isBlank ? "" : normalizationService.normalize(request.getMessage());

        TicketResponse response;
        boolean needsReview;
        long startTime = System.currentTimeMillis();

        Optional<TicketEntity> existing = duplicateCheckService.findExisting(normalizedMessage);

        if (existing.isPresent()) {
            TicketEntity match = existing.get();
            response = duplicateCheckService.toReusedResponse(match);
            needsReview = Boolean.TRUE.equals(match.getNeedsReview());

        } else if (isBlank) {
            response = fallbackService.insufficientInfoResponse();
            needsReview = true;

        } else {
            TicketResponse attempt = null;
            boolean success = false;

            for (int attemptNumber = 1; attemptNumber <= 2 && !success; attemptNumber++) {
                try {
                    attempt = ticketAssistant.classify(request.getMessage());
                    validationService.validateOrThrow(attempt);
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
                response = fallbackService.fallbackResponse();
                needsReview = true;
            }
        }

        long processingTime = System.currentTimeMillis() - startTime;

        TicketEntity entity = new TicketEntity();
        entity.setTitle(request.getTitle());
        entity.setCreatedBy(request.getCreatedBy());
        entity.setEmail(request.getEmail());
        entity.setMessage(request.getMessage());
        entity.setNormalizedMessage(normalizedMessage);
        entity.setCategory(response.getCategory());
        entity.setPriority(response.getPriority());
        entity.setAssignedTeam(response.getAssignedTeam());
        entity.setReason(response.getReason());
        entity.setConfidence(response.getConfidence());
        entity.setProcessingTimeMs(processingTime);
        entity.setNeedsReview(needsReview);
        entity.setCreatedAt(LocalDateTime.now());

        ticketRepository.save(entity);



        return ResponseEntity.ok(response);
    }

    public List<Object> routeTicketsBatch(List<TicketRequest> requests) {
        return requests.stream()
                .map(this::routeTicket)
                .map(ResponseEntity::getBody)
                .map(body -> (Object) body)
                .toList();
    }
}