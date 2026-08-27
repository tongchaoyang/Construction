const API_BASE = "/api";

class ApiError extends Error {
  constructor(message, status, fieldErrors) {
    super(message);
    this.status = status;
    this.fieldErrors = fieldErrors || null;
  }
}

function authHeader() {
  const token = localStorage.getItem("lm_token");
  return token ? { Authorization: "Bearer " + token } : {};
}

async function apiFetch(path, options = {}) {
  const isFormData = options.body instanceof FormData;
  const headers = Object.assign(
    {},
    isFormData ? {} : { "Content-Type": "application/json" },
    authHeader(),
    options.headers || {}
  );

  const response = await fetch(API_BASE + path, {
    ...options,
    headers,
  });

  if (response.status === 204) {
    return null;
  }

  let data = null;
  const text = await response.text();
  if (text) {
    try {
      data = JSON.parse(text);
    } catch (e) {
      data = null;
    }
  }

  if (!response.ok) {
    const message = (data && data.message) || "Request failed (" + response.status + ")";
    throw new ApiError(message, response.status, data && data.fieldErrors);
  }

  return data;
}

const api = {
  get: (path) => apiFetch(path, { method: "GET" }),
  post: (path, body) => apiFetch(path, { method: "POST", body: body instanceof FormData ? body : JSON.stringify(body) }),
  put: (path, body) => apiFetch(path, { method: "PUT", body: body instanceof FormData ? body : JSON.stringify(body) }),
  patch: (path, body) => apiFetch(path, { method: "PATCH", body: JSON.stringify(body) }),
  del: (path) => apiFetch(path, { method: "DELETE" }),
};
