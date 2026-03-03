package com.citynet.model;

import java.time.Instant;

// Alert represents a traffic alert (for example, an incident or severe congestion notification).
// HeapPriorityService stores Alert instances in a max-heap based on severity and recency.
public class Alert {

    private String id;        // Identifier assigned by the client or external system
    private String message;   // Human-readable description of the alert
    private int severity;     // Higher integer means higher priority in the max-heap
    private Instant createdAt; // Timestamp used as a tie-breaker among equal severities

    public Alert() {
        // Default constructor for JSON deserialization.
    }

    public Alert(String id, String message, int severity, Instant createdAt) {
        this.id = id;
        this.message = message;
        this.severity = severity;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getSeverity() {
        return severity;
    }

    public void setSeverity(int severity) {
        this.severity = severity;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

