package com.citynet.service;

import com.citynet.algorithm.PredictiveTrafficSimulator;
import com.citynet.model.TrafficState;
import org.springframework.stereotype.Service;

// TrafficSimulationService exposes a higher-level interface over PredictiveTrafficSimulator.
// Controllers call this service to run recursive predictive simulations over a time horizon.
@Service
public class TrafficSimulationService {

    private final PredictiveTrafficSimulator simulator = new PredictiveTrafficSimulator();

    public PredictiveTrafficSimulator.SimulationResult runSimulation(
            TrafficState initialState,
            int horizon,
            int timeStep,
            double congestionThreshold
    ) {
        return simulator.simulate(initialState, horizon, timeStep, congestionThreshold);
    }
}

