package com.citynet.model;

// Edge represents a directed road segment in the graph used by RecursiveDijkstra.
// The fields expose only structural properties so that real data can be injected from external sources.
public class Edge {

    private String id; // Unique identifier for the road segment (for example, a real-world segment id)
    private String fromIntersectionId; // Source intersection identifier
    private String toIntersectionId;   // Target intersection identifier
    private double baseTravelTime;     // Baseline travel time (e.g., seconds or minutes) without congestion
    private boolean blocked;           // Whether the road is currently blocked for emergency routing

    public Edge() {
        // Default constructor for JSON deserialization.
    }

    public Edge(String id, String fromIntersectionId, String toIntersectionId, double baseTravelTime, boolean blocked) {
        this.id = id;
        this.fromIntersectionId = fromIntersectionId;
        this.toIntersectionId = toIntersectionId;
        this.baseTravelTime = baseTravelTime;
        this.blocked = blocked;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFromIntersectionId() {
        return fromIntersectionId;
    }

    public void setFromIntersectionId(String fromIntersectionId) {
        this.fromIntersectionId = fromIntersectionId;
    }

    public String getToIntersectionId() {
        return toIntersectionId;
    }

    public void setToIntersectionId(String toIntersectionId) {
        this.toIntersectionId = toIntersectionId;
    }

    public double getBaseTravelTime() {
        return baseTravelTime;
    }

    public void setBaseTravelTime(double baseTravelTime) {
        this.baseTravelTime = baseTravelTime;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }
}

