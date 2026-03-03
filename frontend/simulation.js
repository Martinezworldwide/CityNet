// simulation.js connects the predictive traffic simulation UI to the /simulate endpoint.
// It relies on the global getBackendBaseUrl helper defined in dashboard.js.

const simulationPayloadInput = document.getElementById("simulationPayload");
const runSimulationButton = document.getElementById("runSimulationButton");
const simulationOutput = document.getElementById("simulationOutput");

async function runSimulation() {
    const baseUrl = window.getBackendBaseUrl();
    const raw = simulationPayloadInput.value.trim();
    if (!raw) {
        simulationOutput.textContent = "Provide a JSON payload with initialState, horizon, timeStep, and congestionThreshold.";
        return;
    }

    let payload;
    try {
        payload = JSON.parse(raw);
    } catch (e) {
        simulationOutput.textContent = `Invalid JSON payload: ${e}`;
        return;
    }

    try {
        const response = await fetch(`${baseUrl}/simulate`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });
        const json = await response.json();
        simulationOutput.textContent = JSON.stringify(json, null, 2);
    } catch (e) {
        simulationOutput.textContent = `Error running simulation: ${e}`;
    }
}

if (runSimulationButton) {
    runSimulationButton.addEventListener("click", () => {
        runSimulation().catch(() => {});
    });
}

