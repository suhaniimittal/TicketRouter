package com.example.smartticketrouter.repository;

import com.example.smartticketrouter.entity.TicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface TicketRepository extends JpaRepository<TicketEntity,Long> {

    Optional<TicketEntity> findFirstByNormalizedMessageOrderByIdAsc(String normalizedMessage);

    Optional<TicketEntity> findByTicketId(String ticketId);

    // NEW: get all tickets of a given priority
    List<TicketEntity> findByPriority(String priority);

    // NEW: get all tickets assigned to a given team
    List<TicketEntity> findByAssignedTeam(String assignedTeam);


}