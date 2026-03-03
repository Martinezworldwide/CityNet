package com.citynet.controller;

import com.citynet.algorithm.PredictiveTrafficSimulator;
import com.citynet.model.TrafficState;
import com.citynet.service.TrafficSimulationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// SimulationController exposes REST endpoints for predictive traffic simulation.
// It shows how recursive time-stepped prediction can be invoked from a REST API.
@RestController
@RequestMapping("/simulate")
public class SimulationController {

    private final TrafficSimulationService simulationService;

    public SimulationController(TrafficSimulationService simulationService) {
        this.simulationService = simulationService;
    }

    // DTO capturing the POST /simulate request body.
    public record SimulationRequest(
            TrafficState initialState,
            int horizon,
            int timeStep,
            double congestionThreshold
    ) {
    }

    // DTO exposing the simulation response to the frontend.
    public record SimulationResponse(
            java.util.List<TrafficState> timeline,
            java.util.Map<String, String> signalRecommendations
    ) {
    }

    // POST /simulate
    // This endpoint accepts a user-specified initial TrafficState and simulation parameters,
    // then invokes the recursive PredictiveTrafficSimulator.
    @PostMapping
    public ResponseEntity<SimulationResponse> simulate(@RequestBody SimulationRequest request) {
        PredictiveTrafficSimulator.SimulationResult result = simulationService.runSimulation(
                request.initialState(),
                request.horizon(),
                request.timeStep(),
                request.congestionThreshold()
        );
        return ResponseEntity.ok(new SimulationResponse(result.getTimeline(), result.getSignalRecommendations()));
    }
}

