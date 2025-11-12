window.onload = async function() {
  // Load Swagger UI
  const ui = SwaggerUIBundle({
    url: "/v3/api-docs",
    dom_id: "#swagger-ui",
    presets: [SwaggerUIBundle.presets.apis, SwaggerUIStandalonePreset],
    layout: "BaseLayout",
    deepLinking: true,
    persistAuthorization: true
  });

  // Automatically authenticate Swagger
  try {
    const response = await fetch("/api/auth/swagger-login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username: "swagger-admin", password: "swagger@123" })
    });
    if (response.ok) {
      const data = await response.json();
      const token = data.accessToken;
      ui.initOAuth({
        clientId: "swagger-client",
        clientSecret: "swagger-secret"
      });
      ui.preauthorizeApiKey("bearerAuth", "Bearer " + token);
      console.log("✅ Swagger auto-authorized with JWT");
    } else {
      console.warn("⚠️ Failed Swagger auto-login");
    }
  } catch (err) {
    console.error("❌ Error during Swagger auto-login:", err);
  }

  window.ui = ui;
};
