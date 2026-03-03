package com.citynet.service;

import com.citynet.algorithm.RecursiveDijkstra;
import com.citynet.model.Edge;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

// RouteService orchestrates the RecursiveDijkstra algorithm for emergency routing.
// It provides a simple, service-level interface for controllers.
@Service
public class RouteService {

    private final RecursiveDijkstra recursiveDijkstra = new RecursiveDijkstra();

    // Computes the shortest emergency route using RecursiveDijkstra on the provided road network.
    // The network and congestion data are supplied at runtime to avoid embedding synthetic data.
    public RecursiveDijkstra.RouteResult computeEmergencyRoute(
            String startIntersectionId,
            String targetIntersectionId,
            List<Edge> edges,
            Map<String, Double> congestionPenalties
    ) {
        return recursiveDijkstra.findShortestEmergencyRoute(
                startIntersectionId,
                targetIntersectionId,
                edges,
                congestionPenalties
        );
    }
}

