package com.example.smartticketrouter.controller;

import com.example.smartticketrouter.entity.TicketEntity;
import com.example.smartticketrouter.model.TicketRequest;
import com.example.smartticketrouter.model.TicketResponse;
import com.example.smartticketrouter.service.TicketQueryService;
import com.example.smartticketrouter.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tickets")
public class TicketController {

    private final TicketService ticketService;
    private final TicketQueryService ticketQueryService;

    public TicketController(TicketService ticketService, TicketQueryService ticketQueryService) {
        this.ticketService = ticketService;
        this.ticketQueryService = ticketQueryService;
    }

//    @PostMapping("/route")
//    public TicketResponse routeTicket(@Valid @RequestBody TicketRequest request) {
//        return ticketService.routeTicket(request);
//    }

    @PostMapping("/route")
    public ResponseEntity<?> routeTicket(@RequestBody TicketRequest request) {
        return ticketService.routeTicket(request);
    }

    @PostMapping("/route/batch")
    public ResponseEntity<List<Object>> routeTicketsBatch(
            @RequestBody List<TicketRequest> requests) {

        return ResponseEntity.ok(ticketService.routeTicketsBatch(requests));
    }

    @GetMapping
    public List<TicketEntity> getAllTickets() {
        return ticketQueryService.getAllTickets();
    }

    @GetMapping("/{id}")
    public TicketEntity getTicketById(@PathVariable Long id) {
        return ticketQueryService.getTicketById(id);
    }

    @GetMapping("/priority/{priority}")
    public List<TicketEntity> getTicketsByPriority(@PathVariable String priority) {
        return ticketQueryService.getTicketsByPriority(priority);
    }

    @GetMapping("/team/{team}")
    public List<TicketEntity> getTicketsByTeam(@PathVariable String team) {
        return ticketQueryService.getTicketsByTeam(team);
    }
}