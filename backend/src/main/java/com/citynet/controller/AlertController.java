package com.citynet.controller;

import com.citynet.model.Alert;
import com.citynet.service.HeapPriorityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

// AlertController exposes REST endpoints that demonstrate binary heap prioritization of alerts.
@RestController
@RequestMapping("/alerts")
public class AlertController {

    private final HeapPriorityService heapPriorityService;

    public AlertController(HeapPriorityService heapPriorityService) {
        this.heapPriorityService = heapPriorityService;
    }

    // POST /alerts
    // Adds a new alert into the max-heap priority queue.
    @PostMapping
    public ResponseEntity<Alert> createAlert(@RequestBody Alert alert) {
        heapPriorityService.addAlert(alert);
        return ResponseEntity.ok(alert);
    }

    // GET /alerts/next
    // Retrieves the highest-priority alert using max-heap semantics.
    @GetMapping("/next")
    public ResponseEntity<Alert> getNextAlert() {
        Optional<Alert> maybeAlert = heapPriorityService.pollNextAlert();
        return maybeAlert
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}

