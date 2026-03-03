package com.citynet.algorithm;

import com.citynet.model.Edge;

import java.util.*;

// RecursiveDijkstra implements Dijkstra's single-source shortest-path algorithm using recursion.
// The recursion unfolds by repeatedly extracting the next closest node from a min-heap (PriorityQueue)
// and relaxing its outgoing edges until either the target is reached or the heap is empty.
public class RecursiveDijkstra {

    // Public DTO to expose the result of emergency routing as a path and total cost.
    public static class RouteResult {
        private final List<String> path; // Ordered intersection identifiers from start to target
        private final double totalCost;  // Total travel time including congestion penalties

        public RouteResult(List<String> path, double totalCost) {
            this.path = path;
            this.totalCost = totalCost;
        }

        public List<String> getPath() {
            return path;
        }

        public double getTotalCost() {
            return totalCost;
        }
    }

    // Public entry point used by RouteService.
    // edges: directed road segments supplied from real data sources.
    // congestionPenalties: additional cost per edge id (e.g., due to live congestion metrics).
    public RouteResult findShortestEmergencyRoute(
            String startIntersectionId,
            String targetIntersectionId,
            List<Edge> edges,
            Map<String, Double> congestionPenalties
    ) {
        // Build adjacency list indexed by "from" intersection for fast edge lookup.
        Map<String, List<Edge>> adjacency = buildAdjacency(edges);

        // distances holds the best-known distance from start to each intersection.
        Map<String, Double> distances = new HashMap<>();
        // previous keeps track of the predecessor on the current best path.
        Map<String, String> previous = new HashMap<>();

        // Min-heap ordered by current tentative distance from the start.
        PriorityQueue<NodeDistance> queue = new PriorityQueue<>(Comparator.comparingDouble(n -> n.distance));

        // Initialize base case for recursion: the start node has distance 0.
        distances.put(startIntersectionId, 0.0);
        queue.offer(new NodeDistance(startIntersectionId, 0.0));

        // Kick off the recursive Dijkstra exploration.
        dijkstraRecursive(queue, adjacency, distances, previous, congestionPenalties, targetIntersectionId);

        // If we never discovered a finite distance to the target, we return an empty path.
        if (!distances.containsKey(targetIntersectionId)) {
            return new RouteResult(Collections.emptyList(), Double.POSITIVE_INFINITY);
        }

        // Reconstruct the path from target back to start using the "previous" map.
        List<String> path = reconstructPath(startIntersectionId, targetIntersectionId, previous);
        double totalCost = distances.get(targetIntersectionId);
        return new RouteResult(path, totalCost);
    }

    // Recursive step for Dijkstra:
    // - Base case 1: heap empty -> all reachable nodes have been processed.
    // - Base case 2: next node polled is the target -> we can stop early.
    // - Recursive case: relax outgoing edges and recurse with the updated heap and maps.
    private void dijkstraRecursive(
            PriorityQueue<NodeDistance> queue,
            Map<String, List<Edge>> adjacency,
            Map<String, Double> distances,
            Map<String, String> previous,
            Map<String, Double> congestionPenalties,
            String targetIntersectionId
    ) {
        if (queue.isEmpty()) {
            // Base case: no more nodes to process; recursion terminates.
            return;
        }

        NodeDistance current = queue.poll();

        // If we have already found a shorter path to this node, skip processing to avoid redundant work.
        Double knownDistance = distances.get(current.intersectionId);
        if (knownDistance != null && current.distance > knownDistance) {
            dijkstraRecursive(queue, adjacency, distances, previous, congestionPenalties, targetIntersectionId);
            return;
        }

        if (current.intersectionId.equals(targetIntersectionId)) {
            // Base case: the min-heap returned the target as the nearest remaining node,
            // so the shortest path to the target is finalized and we can terminate recursion.
            return;
        }

        // Relax each outgoing edge from the current intersection.
        List<Edge> outgoing = adjacency.getOrDefault(current.intersectionId, Collections.emptyList());
        for (Edge edge : outgoing) {
            if (edge.isBlocked()) {
                // Blocked roads are explicitly ignored for emergency routing.
                continue;
            }

            // Combine base travel time with a dynamic congestion penalty from real-time metrics.
            double penalty = congestionPenalties.getOrDefault(edge.getId(), 0.0);
            double weight = edge.getBaseTravelTime() + penalty;
            double candidateDistance = current.distance + weight;

            Double existingDistance = distances.get(edge.getToIntersectionId());
            if (existingDistance == null || candidateDistance < existingDistance) {
                // This edge improves the shortest path estimate to the neighbor.
                distances.put(edge.getToIntersectionId(), candidateDistance);
                previous.put(edge.getToIntersectionId(), current.intersectionId);
                queue.offer(new NodeDistance(edge.getToIntersectionId(), candidateDistance));
            }
        }

        // Recursive call processes the next closest node chosen by the min-heap.
        dijkstraRecursive(queue, adjacency, distances, previous, congestionPenalties, targetIntersectionId);
    }

    // Helper to convert a flat edge list into an adjacency map.
    private Map<String, List<Edge>> buildAdjacency(List<Edge> edges) {
        Map<String, List<Edge>> adjacency = new HashMap<>();
        for (Edge edge : edges) {
            adjacency
                    .computeIfAbsent(edge.getFromIntersectionId(), key -> new ArrayList<>())
                    .add(edge);
        }
        return adjacency;
    }

    // Reconstruct the path by walking backward from target to start using the "previous" links.
    // This reconstruction is iterative to keep recursion depth limited to the Dijkstra exploration only.
    private List<String> reconstructPath(String start, String target, Map<String, String> previous) {
        List<String> reversed = new ArrayList<>();
        String current = target;
        while (current != null) {
            reversed.add(current);
            if (current.equals(start)) {
                break;
            }
            current = previous.get(current);
        }
        Collections.reverse(reversed);
        if (reversed.isEmpty() || !reversed.get(0).equals(start)) {
            // If the path does not start at the requested source, no valid path exists.
            return Collections.emptyList();
        }
        return reversed;
    }

    // Internal helper used by the min-heap to associate intersections with distances.
    private static class NodeDistance {
        private final String intersectionId;
        private final double distance;

        private NodeDistance(String intersectionId, double distance) {
            this.intersectionId = intersectionId;
            this.distance = distance;
        }
    }
}

