// alerts.js wires the alerts section of the dashboard to the backend heap priority endpoints.
// It relies on the global getBackendBaseUrl helper defined in dashboard.js.

const alertMessageInput = document.getElementById("alertMessage");
const alertSeverityInput = document.getElementById("alertSeverity");
const createAlertButton = document.getElementById("createAlertButton");
const nextAlertButton = document.getElementById("nextAlertButton");
const alertsOutput = document.getElementById("alertsOutput");

async function createAlert() {
    const baseUrl = window.getBackendBaseUrl();
    const message = alertMessageInput.value.trim();
    const severity = parseInt(alertSeverityInput.value, 10);

    if (!message || Number.isNaN(severity)) {
        alertsOutput.textContent = "Provide both a message and a numeric severity.";
        return;
    }

    try {
        const response = await fetch(`${baseUrl}/alerts`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                id: crypto.randomUUID(), // Client-provided identifier; the backend treats it as opaque.
                message,
                severity
            })
        });
        const json = await response.json();
        alertsOutput.textContent = `Stored alert with id=${json.id}, severity=${json.severity}.`;
    } catch (e) {
        alertsOutput.textContent = `Error creating alert: ${e}`;
    }
}

async function getNextAlert() {
    const baseUrl = window.getBackendBaseUrl();
    try {
        const response = await fetch(`${baseUrl}/alerts/next`);
        if (response.status === 204) {
            alertsOutput.textContent = "No alerts in the heap.";
            return;
        }
        const json = await response.json();
        alertsOutput.textContent =
            `Next alert (max-heap): id=${json.id}, severity=${json.severity}, message="${json.message}"`;
    } catch (e) {
        alertsOutput.textContent = `Error fetching next alert: ${e}`;
    }
}

if (createAlertButton) {
    createAlertButton.addEventListener("click", () => {
        createAlert().catch(() => {});
    });
}

if (nextAlertButton) {
    nextAlertButton.addEventListener("click", () => {
        getNextAlert().catch(() => {});
    });
}

