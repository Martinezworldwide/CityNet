// dashboard.js centralizes access to the configured backend base URL.
// This file defines a global helper so it can be used without ES module support on GitHub Pages.

// Returns the backend base URL configured in the input field.
function getBackendBaseUrl() {
    const input = document.getElementById("backendBaseUrl");
    const isGithubPages = typeof window !== "undefined"
        && window.location
        && window.location.hostname === "martinezworldwide.github.io";

    if (isGithubPages) {
        // On the published GitHub Pages site, always use the deployed Render backend URL
        // so instructors do not need to paste it manually.
        const fixedUrl = "https://citynet-backend.onrender.com";
        if (input) {
            input.value = fixedUrl;
        }
        return fixedUrl;
    }

    if (!input) {
        // Fallback to localhost when running outside GitHub Pages.
        return "http://localhost:8886";
    }
    // Normalize the value and strip any trailing slashes so that fetch URLs
    // do not accidentally contain "//alerts" or similar paths.
    const raw = input.value.trim() || "http://localhost:8886";
    return raw.replace(/\/+$/, "");
}

// Expose the helper on window so other scripts can call it.
window.getBackendBaseUrl = getBackendBaseUrl;


