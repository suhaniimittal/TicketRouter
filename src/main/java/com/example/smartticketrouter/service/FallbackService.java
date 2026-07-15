package com.example.smartticketrouter.service;

import com.example.smartticketrouter.model.TicketResponse;
import org.springframework.stereotype.Service;

@Service
public class FallbackService {

    // Used when the AI fails validation/errors out twice in a row
    public TicketResponse fallbackResponse() {
        TicketResponse r = new TicketResponse();
        r.setCategory("General");
        r.setPriority("Medium");
        r.setAssignedTeam("Customer Care");
        r.setReason("Automatic classification failed; routed for manual review.");
        r.setConfidence(0.0);
        return r;
    }

    // Used when the message itself is blank
    public TicketResponse insufficientInfoResponse() {
        TicketResponse r = new TicketResponse();
        r.setCategory("General");
        r.setPriority("Low");
        r.setAssignedTeam("Customer Care");
        r.setReason("No message content provided.");
        r.setConfidence(0.0);
        return r;
    }
}
