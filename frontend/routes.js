// routes.js connects the emergency routing UI to the RecursiveDijkstra-backed endpoint.
// It relies on the global getBackendBaseUrl helper defined in dashboard.js.

const routeStartInput = document.getElementById("routeStart");
const routeTargetInput = document.getElementById("routeTarget");
const requestRouteButton = document.getElementById("requestRouteButton");
const routesOutput = document.getElementById("routesOutput");

async function requestEmergencyRoute() {
    const baseUrl = window.getBackendBaseUrl();
    const start = routeStartInput.value.trim();
    const target = routeTargetInput.value.trim();

    if (!start || !target) {
        routesOutput.textContent = "Provide both start and target intersection identifiers.";
        return;
    }

    try {
        const response = await fetch(`${baseUrl}/routes/emergency?start=${encodeURIComponent(start)}&target=${encodeURIComponent(target)}`);
        const json = await response.json();
        if (!response.ok || !Array.isArray(json.path) || json.path.length === 0) {
            routesOutput.textContent = "No route available. Ensure a real network is configured on the backend.";
            return;
        }
        routesOutput.textContent = `Path: ${json.path.join(" -> ")} (cost=${json.totalCost})`;
    } catch (e) {
        routesOutput.textContent = `Error requesting route: ${e}`;
    }
}

if (requestRouteButton) {
    requestRouteButton.addEventListener("click", () => {
        requestEmergencyRoute().catch(() => {});
    });
}

