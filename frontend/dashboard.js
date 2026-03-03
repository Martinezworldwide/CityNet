// dashboard.js centralizes access to the configured backend base URL.
// This file defines a global helper so it can be used without ES module support on GitHub Pages.

// Returns the backend base URL configured in the input field.
function getBackendBaseUrl() {
    const input = document.getElementById("backendBaseUrl");
    if (!input) {
        // Fallback to localhost if the element is missing; this is safe and does not fabricate data.
        return "http://localhost:8886";
    }
    return input.value.trim() || "http://localhost:8886";
}

// Expose the helper on window so other scripts can call it.
window.getBackendBaseUrl = getBackendBaseUrl;


