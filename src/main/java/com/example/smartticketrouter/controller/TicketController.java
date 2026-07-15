package com.example.smartticketrouter.controller;

import com.example.smartticketrouter.entity.TicketEntity;
import com.example.smartticketrouter.model.TicketRequest;
import com.example.smartticketrouter.model.TicketResponse;
import com.example.smartticketrouter.service.TicketService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tickets")
public class TicketController {

    private final TicketService ticketService;  // this controller needs TicketService

    // constructor injection (spring boot creates TicketService object and gives to controller)
    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;

    }

    @PostMapping("/route/batch")
    public List<TicketResponse> routeTicketsBatch(@RequestBody List<TicketRequest> requests) {
        return ticketService.routeTicketsBatch(requests);
    }

    @PostMapping("/route")
    public TicketResponse routeTicket(@RequestBody TicketRequest request) throws Exception {
        return ticketService.routeTicket(request);
    }

    @GetMapping("/{ticketId}")
    public TicketEntity getTicketById(@PathVariable String ticketId) {
        return ticketService.getTicketById(ticketId);
    }

    @GetMapping("/priority/{priority}")
    public List<TicketEntity> getTicketsByPriority(@PathVariable String priority) {
        return ticketService.getTicketsByPriority(priority);
    }

    @GetMapping("/team/{team}")
    public List<TicketEntity> getTicketsByTeam(@PathVariable String team) {
        return ticketService.getTicketsByTeam(team);
    }

    @GetMapping
    public List<TicketEntity> getAllTickets() {
        return ticketService.getAllTickets();
    }
}