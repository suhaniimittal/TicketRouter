package com.example.smartticketrouter.service;

import com.example.smartticketrouter.entity.TicketEntity;
import com.example.smartticketrouter.repository.TicketRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class TicketQueryService {

    private final TicketRepository ticketRepository;

    public TicketQueryService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public List<TicketEntity> getAllTickets() {
        return ticketRepository.findAll();
    }

    public TicketEntity getTicketById(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No ticket found with ID: " + id));
    }

    public List<TicketEntity> getTicketsByPriority(String priority) {
        return ticketRepository.findByPriorityIgnoreCase(priority);
    }

    public List<TicketEntity> getTicketsByTeam(String assignedTeam) {
        return ticketRepository.findByAssignedTeamIgnoreCase(assignedTeam);
    }
}