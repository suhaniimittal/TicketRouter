package com.example.smartticketrouter.service;

import org.springframework.stereotype.Service;

@Service
public class NormalizationService {

    // Turns messy real-world text into a consistent, comparable form
    public String normalize(String message) {
        if (message == null) return "";
        return message
                .trim()
                .toLowerCase()
                .replaceAll("\\s+", " ")
                .replaceAll("[!?.,]+$", "");
    }
}
