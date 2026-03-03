// dashboard.js centralizes access to the configured backend base URL.
// Other frontend modules import getBackendBaseUrl to call REST endpoints.

// Returns the backend base URL configured in the input field.
export function getBackendBaseUrl() {
    const input = document.getElementById("backendBaseUrl");
    if (!input) {
        // Fallback to localhost if the element is missing; this is safe and does not fabricate data.
        return "http://localhost:8080";
    }
    return input.value.trim() || "http://localhost:8080";
}

