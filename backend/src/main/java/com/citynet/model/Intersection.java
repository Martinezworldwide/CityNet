package com.citynet.model;

// Intersection represents a junction in the road network graph.
// It intentionally keeps only an identifier to avoid embedding any synthetic location data.
public class Intersection {

    private String id; // Unique identifier for the intersection (for example, a real-world intersection code)

    public Intersection() {
        // Default constructor required for JSON deserialization.
    }

    public Intersection(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}

