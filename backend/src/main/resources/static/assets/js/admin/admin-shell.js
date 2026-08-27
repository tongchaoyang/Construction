function renderAdminShell(activePage) {
  if (!Auth.requireAdminOrRedirect()) return false;

  const header = document.getElementById("admin-header");
  if (header) {
    header.classList.add("site-header");
    header.innerHTML = `
      <div class="container">
        <a href="/index.html" class="brand">
          <img src="/assets/images/logo.png" alt="logo">
          <span class="brand-name">Landmark Construction &amp; ADU<small>Admin Portal</small></span>
        </a>
        <div class="nav-actions">
          <span style="color:#dfe6f2;">${Auth.getUser().fullName}</span>
          <a href="/change-password.html" class="btn btn-outline-light btn-sm">Change Password</a>
          <button class="btn btn-outline-light btn-sm" id="admin-logout-btn">Log out</button>
        </div>
      </div>
    `;
    document.getElementById("admin-logout-btn").addEventListener("click", () => {
      Auth.logout();
      window.location.href = "/admin/admin-login.html";
    });
  }

  const sidebar = document.getElementById("admin-sidebar");
  if (sidebar) {
    const link = (page, href, label) =>
      `<a href="${href}" class="${activePage === page ? "active" : ""}">${label}</a>`;
    sidebar.innerHTML = `
      ${link("dashboard", "/admin/admin-dashboard.html", "Dashboard")}
      ${link("projects", "/admin/admin-projects.html", "Projects")}
      ${link("quotes", "/admin/admin-quotes.html", "Quote Requests")}
    `;
  }
  return true;
}

function statusPillHtml(status) {
  return `<span class="status-pill ${status}">${status.replace("_", " ")}</span>`;
}

// A 401 means the token is missing/expired (the locally-cached user/role can go stale
// even though the server has since rejected the token) — send the admin back to log in
// instead of leaving a broken page with a raw "Request failed (401)" message on screen.
// Returns true if it handled (and redirected for) an expired session.
function isAuthExpired(error) {
  if (error && error.status === 401) {
    Auth.logout();
    window.location.href = "/admin/admin-login.html?expired=1";
    return true;
  }
  return false;
}

function handleAdminApiError(error, container) {
  if (isAuthExpired(error)) return;
  container.innerHTML = `<div class="alert alert-error">${error.message}</div>`;
}
