package com.citynet.algorithm;

import com.citynet.model.State;
import com.citynet.model.TrafficState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// PredictiveTrafficSimulator recursively advances a TrafficState over a finite time horizon.
// At each recursive step, it:
// - Creates the next TrafficState by updating congestion and signal settings.
// - Appends the new State snapshot to the history.
// - Terminates when the horizon is reached or a stability condition is detected.
public class PredictiveTrafficSimulator {

    // Result DTO returned to clients: the evolving timeline and high-level signal recommendations.
    public static class SimulationResult {
        private final List<TrafficState> timeline;              // Sequence of traffic states from t to t + horizon
        private final Map<String, String> signalRecommendations; // Per-intersection signal changes suggested at horizon

        public SimulationResult(List<TrafficState> timeline, Map<String, String> signalRecommendations) {
            this.timeline = timeline;
            this.signalRecommendations = signalRecommendations;
        }

        public List<TrafficState> getTimeline() {
            return timeline;
        }

        public Map<String, String> getSignalRecommendations() {
            return signalRecommendations;
        }
    }

    // Public entry point used by TrafficSimulationService.
    // The initialState and parameters must be derived from real data or user input at runtime.
    public SimulationResult simulate(
            TrafficState initialState,
            int horizon,
            int timeStep,
            double congestionThreshold
    ) {
        List<TrafficState> timeline = new ArrayList<>();
        // Kick off recursion from the provided initial state.
        simulateRecursive(initialState, horizon, timeStep, congestionThreshold, timeline);

        // After the horizon, derive consolidated signal recommendations from the last state.
        Map<String, String> recommendations = deriveSignalRecommendations(
                timeline.isEmpty() ? initialState : timeline.get(timeline.size() - 1),
                congestionThreshold
        );
        return new SimulationResult(timeline, recommendations);
    }

    // Recursive time-stepped simulation:
    // - Base case 1: current time has reached or exceeded the horizon.
    // - Base case 2: congestion has fallen below the threshold for all intersections (system stable).
    // - Recursive case: compute next state and call simulateRecursive again for the next time step.
    private void simulateRecursive(
            TrafficState current,
            int horizon,
            int timeStep,
            double congestionThreshold,
            List<TrafficState> timeline
    ) {
        timeline.add(current);

        if (current.getCurrentTime() >= horizon) {
            // Base case: time horizon reached; recursion terminates.
            return;
        }

        if (allCongestionBelowThreshold(current.getCongestionByIntersection(), congestionThreshold)) {
            // Base case: every intersection is under the congestion threshold, so further simulation
            // would not meaningfully change routing or signal decisions.
            return;
        }

        // Compute the next TrafficState based on simple, deterministic congestion dynamics.
        TrafficState next = computeNextState(current, timeStep, congestionThreshold);

        // Recursive call on the next time slice.
        simulateRecursive(next, horizon, timeStep, congestionThreshold, timeline);
    }

    // Simple deterministic rule for congestion evolution and signal adjustments.
    // This keeps the focus on recursive structure rather than domain-specific calibration.
    private TrafficState computeNextState(
            TrafficState current,
            int timeStep,
            double congestionThreshold
    ) {
        Map<String, Double> currentCongestion = current.getCongestionByIntersection();
        Map<String, String> currentSignals = current.getSignalByIntersection();

        Map<String, Double> nextCongestion = new HashMap<>();
        Map<String, String> nextSignals = new HashMap<>();

        for (Map.Entry<String, Double> entry : currentCongestion.entrySet()) {
            String intersectionId = entry.getKey();
            double level = entry.getValue();

            // Example deterministic rule:
            // - If signal is favoring this intersection, reduce congestion.
            // - Otherwise, congestion decays slowly but never increases artificially.
            String signal = currentSignals != null ? currentSignals.get(intersectionId) : null;
            double adjusted;
            if ("GREEN".equalsIgnoreCase(signal)) {
                adjusted = Math.max(0.0, level * 0.8); // Faster decay under a green phase.
            } else {
                adjusted = Math.max(0.0, level * 0.95); // Slow natural decay from vehicles clearing.
            }

            nextCongestion.put(intersectionId, adjusted);

            // Signal policy: if congestion still exceeds threshold, keep or turn it GREEN,
            // otherwise relax it to "BALANCED".
            if (adjusted > congestionThreshold) {
                nextSignals.put(intersectionId, "GREEN");
            } else {
                nextSignals.put(intersectionId, "BALANCED");
            }
        }

        // Extend history with a snapshot of the new state for analysis.
        List<State> nextHistory = current.getHistory() == null
                ? new ArrayList<>()
                : new ArrayList<>(current.getHistory());
        nextHistory.add(new State(
                current.getCurrentTime() + timeStep,
                new HashMap<>(nextCongestion)
        ));

        return new TrafficState(
                current.getCurrentTime() + timeStep,
                nextCongestion,
                nextSignals,
                nextHistory
        );
    }

    // Utility to check whether the system is globally below the congestion threshold.
    private boolean allCongestionBelowThreshold(Map<String, Double> congestionByIntersection, double threshold) {
        if (congestionByIntersection == null || congestionByIntersection.isEmpty()) {
            // If we have no data, we conservatively stop the recursion.
            return true;
        }
        for (double value : congestionByIntersection.values()) {
            if (value > threshold) {
                return false;
            }
        }
        return true;
    }

    // Derive a simple set of signal recommendations from the final TrafficState.
    // This is separate from the per-step update rule to clearly illustrate the analysis phase
    // after the recursive prediction has completed.
    private Map<String, String> deriveSignalRecommendations(TrafficState finalState, double congestionThreshold) {
        Map<String, String> recommendations = new HashMap<>();
        Map<String, Double> congestion = finalState.getCongestionByIntersection();
        if (congestion == null) {
            return recommendations;
        }
        for (Map.Entry<String, Double> entry : congestion.entrySet()) {
            String intersectionId = entry.getKey();
            double level = entry.getValue();
            if (level > congestionThreshold) {
                recommendations.put(intersectionId, "EXTEND_GREEN");
            } else {
                recommendations.put(intersectionId, "NORMAL_CYCLE");
            }
        }
        return recommendations;
    }
}

