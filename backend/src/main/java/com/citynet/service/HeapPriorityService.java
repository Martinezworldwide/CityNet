package com.citynet.service;

import com.citynet.model.Alert;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.Optional;
import java.util.PriorityQueue;

// HeapPriorityService maintains a max-heap of Alert objects.
// It uses Java's PriorityQueue with a custom Comparator so that:
// - Higher severity alerts are returned first.
// - Among equal severity alerts, newer alerts (larger createdAt) are prioritized.
@Service
public class HeapPriorityService {

    private final PriorityQueue<Alert> maxHeap;

    public HeapPriorityService() {
        // Configure the PriorityQueue as a max-heap by reversing the comparator.
        this.maxHeap = new PriorityQueue<>(
                Comparator
                        .comparingInt(Alert::getSeverity)
                        .thenComparing(Alert::getCreatedAt)
                        .reversed()
        );
    }

    // Adds an alert into the max-heap, ensuring createdAt is populated if absent.
    public synchronized void addAlert(Alert alert) {
        if (alert.getCreatedAt() == null) {
            // Fill in the timestamp if the client did not set it; this is metadata, not domain data.
            alert.setCreatedAt(Instant.now());
        }
        maxHeap.offer(alert);
    }

    // Retrieves and removes the highest-priority alert from the heap, if any.
    public synchronized Optional<Alert> pollNextAlert() {
        Alert alert = maxHeap.poll();
        return Optional.ofNullable(alert);
    }
}

