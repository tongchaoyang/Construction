function renderHeader() {
  const header = document.getElementById("site-header");
  if (!header) return;
  header.classList.add("site-header");
  const activePage = document.body.getAttribute("data-page") || "";
  const loggedIn = Auth.isLoggedIn();
  const user = Auth.getUser();

  const navLink = (page, href, key, label) =>
    `<a href="${href}" class="${activePage === page ? "active" : ""}" data-i18n="${key}">${label}</a>`;

  const adminLink = loggedIn && user.role === "ADMIN"
    ? `<a href="/admin/admin-dashboard.html" class="btn btn-primary btn-sm" data-i18n="nav.adminPortal">Admin Portal</a>`
    : "";

  const authArea = loggedIn
    ? `<span style="color:#dfe6f2;font-size:0.9rem;">${escapeHtml(user.fullName)}</span>
       ${adminLink}
       <a href="/change-password.html" class="btn btn-outline-light btn-sm" data-i18n="nav.changePassword">Change Password</a>
       <button class="btn btn-outline-light btn-sm" id="nav-logout-btn" data-i18n="nav.logout">Log out</button>`
    : `<a href="/login.html" class="btn btn-outline-light btn-sm" data-i18n="nav.login">Log in</a>
       <a href="/register.html" class="btn btn-primary btn-sm" data-i18n="nav.register">Sign up</a>`;

  header.innerHTML = `
    <div class="container">
      <a href="/index.html" class="brand">
        <img src="/assets/images/logo.png" alt="Landmark Construction & ADU logo">
        <span class="brand-name">Landmark Construction &amp; ADU<small>里程建筑 · CSLB #1140588</small></span>
      </a>
      <nav class="main-nav">
        ${navLink("home", "/index.html", "nav.home", "Home")}
        ${navLink("projects", "/projects.html", "nav.projects", "Projects")}
        ${navLink("quote", "/quote-request.html", "nav.quote", "Request a Quote")}
        ${navLink("about", "/about.html", "nav.about", "About")}
      </nav>
      <div class="nav-actions">
        <button class="lang-toggle" id="lang-toggle-btn"><span data-lang-label>中文</span></button>
        ${authArea}
      </div>
    </div>
  `;

  const logoutBtn = document.getElementById("nav-logout-btn");
  if (logoutBtn) {
    logoutBtn.addEventListener("click", () => {
      Auth.logout();
      window.location.href = "/index.html";
    });
  }

  const langBtn = document.getElementById("lang-toggle-btn");
  if (langBtn) {
    langBtn.addEventListener("click", () => {
      const next = I18n.current() === "zh" ? "en" : "zh";
      I18n.setLanguage(next);
    });
  }
}

function renderFooter() {
  const footer = document.getElementById("site-footer");
  if (!footer) return;
  footer.classList.add("site-footer");
  footer.innerHTML = `
    <div class="container">
      <div class="footer-grid">
        <div>
          <h4>Landmark Construction &amp; ADU<br>里程建筑</h4>
          <p data-i18n="footer.tagline">Residential construction, ADU, remodeling &amp; earthquake retrofit in Silicon Valley.</p>
        </div>
        <div>
          <h4 data-i18n="footer.contact">Contact</h4>
          <p>97 E Brokaw Rd, STE 110<br>San Jose, CA 95112</p>
          <p><a href="tel:4089159235">408-915-9235</a></p>
          <p><a href="mailto:landmarkadu@gmail.com">landmarkadu@gmail.com</a></p>
        </div>
        <div>
          <h4 data-i18n="footer.links">Quick Links</h4>
          <p><a href="/projects.html" data-i18n="nav.projects">Projects</a></p>
          <p><a href="/quote-request.html" data-i18n="nav.quote">Request a Quote</a></p>
          <p><a href="/about.html" data-i18n="nav.about">About</a></p>
          <p><a href="/admin/admin-login.html" data-i18n="footer.adminLogin">Admin Login</a></p>
        </div>
        <div>
          <h4 data-i18n="footer.license">License</h4>
          <p data-i18n="footer.licenseText">CSLB License #1140588 (Class B - General Building)</p>
        </div>
      </div>
      <div class="footer-bottom">
        <span data-i18n="footer.copyright">&copy; 2026 Landmark Construction &amp; ADU. All rights reserved.</span>
      </div>
    </div>
  `;
}

function escapeHtml(str) {
  const div = document.createElement("div");
  div.textContent = str == null ? "" : String(str);
  return div.innerHTML;
}

document.addEventListener("DOMContentLoaded", () => {
  renderHeader();
  renderFooter();
  document.addEventListener("i18n:ready", () => I18n.apply());
  document.addEventListener("i18n:changed", () => I18n.apply());
});
