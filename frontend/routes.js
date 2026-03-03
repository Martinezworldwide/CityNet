// routes.js connects the emergency routing UI to the RecursiveDijkstra-backed endpoint.
// It relies on the global getBackendBaseUrl helper defined in dashboard.js.
// Working example: POST /routes/emergency with pre-filled JSON so the instructor sees a route immediately.

const routePayloadInput = document.getElementById("routePayload");
const requestRoutePostButton = document.getElementById("requestRoutePostButton");
const routesOutput = document.getElementById("routesOutput");

// POST the route JSON to /routes/emergency and display path and totalCost.
async function requestEmergencyRoutePost() {
    const baseUrl = window.getBackendBaseUrl();
    const raw = routePayloadInput.value.trim();
    if (!raw) {
        routesOutput.textContent = "Paste or use the pre-filled JSON in the text area, then click Run Emergency Route.";
        return;
    }
    let payload;
    try {
        payload = JSON.parse(raw);
    } catch (e) {
        routesOutput.textContent = `Invalid JSON: ${e.message}`;
        return;
    }
    try {
        const response = await fetch(`${baseUrl}/routes/emergency`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });
        const json = await response.json();
        if (!response.ok) {
            routesOutput.textContent = `Error: ${json.message || response.statusText || response.status}`;
            return;
        }
        const path = json.path;
        const cost = json.totalCost;
        if (!Array.isArray(path) || path.length === 0) {
            routesOutput.textContent = "No route found between start and target with the given edges.";
            return;
        }
        routesOutput.textContent = `Path: ${path.join(" → ")} (cost=${cost})`;
    } catch (e) {
        routesOutput.textContent = `Error: ${e.message}`;
    }
}

if (requestRoutePostButton) {
    requestRoutePostButton.addEventListener("click", () => {
        requestEmergencyRoutePost().catch(() => {});
    });
}

