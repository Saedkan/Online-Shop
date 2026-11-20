(() => {
  let accessToken = null;
  let currentOrderId = null;

  const loginPanel = document.getElementById("login-panel");
  const sessionPanel = document.getElementById("session-panel");
  const resultPanel = document.getElementById("result-panel");
  const resultBody = document.getElementById("result-body");
  const sessionSummary = document.getElementById("session-summary");
  const orderHint = document.getElementById("order-hint");

  function correlationId() {
    return crypto.randomUUID();
  }

  function showResult(text, tone) {
    resultPanel.classList.remove("hidden", "error", "warn");
    if (tone) {
      resultPanel.classList.add(tone);
    }
    resultBody.textContent = text;
  }

  function formatError(status, body) {
    if (body && typeof body === "object" && body.code) {
      return [
        `HTTP ${status}`,
        `code: ${body.code}`,
        `message: ${body.message}`,
        `correlationId: ${body.correlationId}`
      ].join("\n");
    }
    return `HTTP ${status}\n${typeof body === "string" ? body : JSON.stringify(body, null, 2)}`;
  }

  async function api(path, options = {}) {
    const headers = Object.assign(
      { "Content-Type": "application/json", "X-Correlation-Id": correlationId() },
      options.headers || {}
    );
    if (accessToken) {
      headers.Authorization = `Bearer ${accessToken}`;
    }
    const response = await fetch(path, Object.assign({}, options, { headers }));
    let body = null;
    const raw = await response.text();
    if (raw) {
      try {
        body = JSON.parse(raw);
      } catch {
        body = raw;
      }
    }
    return { response, body };
  }

  function setLoggedOut() {
    accessToken = null;
    currentOrderId = null;
    loginPanel.classList.remove("hidden");
    sessionPanel.classList.add("hidden");
    document.getElementById("password").value = "";
  }

  async function refreshSession(expiresIn) {
    const { response, body } = await api("/api/me");
    if (!response.ok) {
      showResult(formatError(response.status, body), "error");
      setLoggedOut();
      return;
    }
    const roles = (body.authorities || []).join(", ");
    sessionSummary.textContent =
      `username: ${body.username}\n` +
      `authorities: ${roles}\n` +
      `expiresIn: ${expiresIn} seconds (15 minutes)`;
    loginPanel.classList.add("hidden");
    sessionPanel.classList.remove("hidden");
  }

  document.getElementById("login-form").addEventListener("submit", async (event) => {
    event.preventDefault();
    const username = document.getElementById("username").value.trim();
    const password = document.getElementById("password").value;

    const { response, body } = await api("/api/v1/auth/login", {
      method: "POST",
      body: JSON.stringify({ username, password })
    });

    if (!response.ok) {
      accessToken = null;
      showResult(formatError(response.status, body), "error");
      return;
    }

    accessToken = body.accessToken;
    await refreshSession(body.expiresIn);
    showResult(`Logged in. Token kept in memory only (not shown).\nexpiresIn=${body.expiresIn}`, null);
  });

  document.getElementById("btn-create").addEventListener("click", async () => {
    const key = `ORD-${Date.now().toString().slice(-6)}`;
    const { response, body } = await api("/api/v1/orders", {
      method: "POST",
      body: JSON.stringify({ businessKey: key, title: "Auth desk order" })
    });

    if (!response.ok) {
      showResult(formatError(response.status, body), response.status === 401 ? "error" : "warn");
      return;
    }

    currentOrderId = body.id;
    orderHint.textContent = `Last order ${body.id} · status ${body.status}. Try Pay next.`;
    showResult(
      `HTTP ${response.status}\n` +
      `id: ${body.id}\n` +
      `businessKey: ${body.businessKey}\n` +
      `status: ${body.status}`,
      null
    );
  });

  document.getElementById("btn-read").addEventListener("click", async () => {
    if (!currentOrderId) {
      showResult("Create an order first.", "warn");
      return;
    }
    const { response, body } = await api(`/api/v1/orders/${currentOrderId}`);
    if (!response.ok) {
      showResult(formatError(response.status, body), "error");
      return;
    }
    showResult(
      `HTTP ${response.status}\n` +
      `id: ${body.id}\n` +
      `businessKey: ${body.businessKey}\n` +
      `title: ${body.title}\n` +
      `status: ${body.status}`,
      null
    );
  });

  document.getElementById("btn-update").addEventListener("click", async () => {
    if (!currentOrderId) {
      showResult("Create an order first.", "warn");
      return;
    }
    const title = `Updated ${new Date().toLocaleTimeString()}`;
    const { response, body } = await api(`/api/v1/orders/${currentOrderId}`, {
      method: "PUT",
      body: JSON.stringify({ title })
    });
    if (!response.ok) {
      showResult(formatError(response.status, body), "error");
      return;
    }
    orderHint.textContent = `Last order ${body.id} · status ${body.status} · title ${body.title}`;
    showResult(
      `HTTP ${response.status}\n` +
      `id: ${body.id}\n` +
      `title: ${body.title}\n` +
      `status: ${body.status}`,
      null
    );
  });

  document.getElementById("btn-delete").addEventListener("click", async () => {
    if (!currentOrderId) {
      showResult("Create an order first.", "warn");
      return;
    }
    const id = currentOrderId;
    const { response, body } = await api(`/api/v1/orders/${id}`, { method: "DELETE" });
    if (!response.ok) {
      showResult(formatError(response.status, body), "error");
      return;
    }
    currentOrderId = null;
    orderHint.textContent = "Order deleted. Create a new one to continue.";
    showResult(`HTTP ${response.status} — order ${id} deleted`, null);
  });

  document.getElementById("btn-pay").addEventListener("click", async () => {
    if (!currentOrderId) {
      showResult("Create an order first so there is an id to pay.", "warn");
      return;
    }

    const before = currentOrderId;
    const { response, body } = await api(`/api/v1/orders/${before}/pay`, { method: "POST" });

    if (response.status === 403) {
      const { response: readResponse, body: readBody } = await api(`/api/v1/orders/${before}`);
      const statusLine = readResponse.ok
        ? `row status still: ${readBody.status}`
        : "could not re-read order";
      showResult(`${formatError(response.status, body)}\n${statusLine}`, "error");
      return;
    }

    if (!response.ok) {
      showResult(formatError(response.status, body), "error");
      return;
    }

    showResult(
      `HTTP ${response.status}\n` +
      `id: ${body.id}\n` +
      `status: ${body.status} (paid)`,
      null
    );
  });

  document.getElementById("btn-logout").addEventListener("click", () => {
    setLoggedOut();
    showResult("Logged out. Token dropped. Next API call will be 401.", "warn");
  });
})();
