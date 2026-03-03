package com.citynet.model;

import java.util.Map;

// State captures traffic-related metrics at a single time step for one or more intersections.
// It is used by PredictiveTrafficSimulator to propagate congestion recursively over time.
public class State {

    private int time; // Discrete time index (for example, minutes from simulation start)

    // Map from intersection identifier to a congestion level (e.g., vehicles per lane).
    // The actual numeric values must come from real data or user input at runtime.
    private Map<String, Double> congestionByIntersection;

    public State() {
        // Default constructor for JSON deserialization.
    }

    public State(int time, Map<String, Double> congestionByIntersection) {
        this.time = time;
        this.congestionByIntersection = congestionByIntersection;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public Map<String, Double> getCongestionByIntersection() {
        return congestionByIntersection;
    }

    public void setCongestionByIntersection(Map<String, Double> congestionByIntersection) {
        this.congestionByIntersection = congestionByIntersection;
    }
}

