package com.citynet.model;

import java.util.List;
import java.util.Map;

// TrafficState represents the system-level view of traffic at a particular time slice.
// It is the unit that the PredictiveTrafficSimulator evolves recursively across the time horizon.
public class TrafficState {

    private int currentTime; // Current simulation time index

    // Per-intersection congestion levels (e.g., queue lengths or flow rates) keyed by intersection identifier.
    private Map<String, Double> congestionByIntersection;

    // Per-intersection signal settings, such as "GREEN_MAIN", "RED_ALL", etc.
    // The actual codes and semantics must be driven by real deployment policies.
    private Map<String, String> signalByIntersection;

    // Optional history of raw states used to illustrate recursion output.
    private List<State> history;

    public TrafficState() {
        // Default constructor for JSON deserialization.
    }

    public TrafficState(int currentTime,
                        Map<String, Double> congestionByIntersection,
                        Map<String, String> signalByIntersection,
                        List<State> history) {
        this.currentTime = currentTime;
        this.congestionByIntersection = congestionByIntersection;
        this.signalByIntersection = signalByIntersection;
        this.history = history;
    }

    public int getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(int currentTime) {
        this.currentTime = currentTime;
    }

    public Map<String, Double> getCongestionByIntersection() {
        return congestionByIntersection;
    }

    public void setCongestionByIntersection(Map<String, Double> congestionByIntersection) {
        this.congestionByIntersection = congestionByIntersection;
    }

    public Map<String, String> getSignalByIntersection() {
        return signalByIntersection;
    }

    public void setSignalByIntersection(Map<String, String> signalByIntersection) {
        this.signalByIntersection = signalByIntersection;
    }

    public List<State> getHistory() {
        return history;
    }

    public void setHistory(List<State> history) {
        this.history = history;
    }
}

