package com.example.smartticketrouter.service;

import com.example.smartticketrouter.entity.TicketEntity;
import com.example.smartticketrouter.model.TicketResponse;
import com.example.smartticketrouter.repository.TicketRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DuplicateCheckService {

    private final TicketRepository ticketRepository;

    public DuplicateCheckService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    // Looks for an earlier ticket with the same normalized message.
    public Optional<TicketEntity> findExisting(String normalizedMessage) {
        if (normalizedMessage == null || normalizedMessage.isBlank()) {
            return Optional.empty();
        }
        return ticketRepository.findFirstByNormalizedMessageOrderByIdAsc(normalizedMessage);
    }

    // Converts a matched past ticket into a reusable response, without calling the AI.
    public TicketResponse toReusedResponse(TicketEntity match) {
        TicketResponse response = new TicketResponse();
        response.setCategory(match.getCategory());
        response.setPriority(match.getPriority());
        response.setAssignedTeam(match.getAssignedTeam());
        response.setReason(match.getReason() + " (reused from ticket " + match.getId() + ")");
        response.setConfidence(match.getConfidence());
        return response;
    }
}