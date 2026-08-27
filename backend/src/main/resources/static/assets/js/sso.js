async function renderSsoButtons(containerId) {
  const container = document.getElementById(containerId);
  if (!container) return;
  try {
    const providers = await api.get("/auth/sso-providers");
    if (!providers.google && !providers.oidc) {
      return;
    }
    let html = `<div style="display:flex;align-items:center;gap:10px;margin:18px 0;color:var(--color-muted);font-size:0.85rem;">
      <hr style="flex:1;border:none;border-top:1px solid var(--color-border);">
      <span data-i18n="sso.divider">or</span>
      <hr style="flex:1;border:none;border-top:1px solid var(--color-border);">
    </div>`;
    if (providers.google) {
      html += `<a href="/oauth2/authorization/google" class="btn btn-secondary" style="width:100%;display:block;margin-bottom:10px;">
        <span data-i18n="sso.signInWithGoogle">Sign in with Google</span>
      </a>`;
    }
    if (providers.oidc) {
      html += `<a href="/oauth2/authorization/oidc" class="btn btn-secondary" style="width:100%;display:block;">
        ${escapeHtmlBasicSso("Sign in with " + providers.oidcDisplayName)}
      </a>`;
    }
    container.innerHTML = html;
    if (typeof I18n !== "undefined") {
      I18n.apply(container);
    }
  } catch (e) {
    // If the endpoint fails for any reason, simply show no SSO options rather than breaking the page.
  }
}

function escapeHtmlBasicSso(str) {
  const div = document.createElement("div");
  div.textContent = str == null ? "" : String(str);
  return div.innerHTML;
}
