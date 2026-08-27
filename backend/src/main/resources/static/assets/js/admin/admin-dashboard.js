async function loadDashboard() {
  if (!renderAdminShell("dashboard")) return;

  const statCards = document.getElementById("stat-cards");
  const tbody = document.querySelector("#recent-quotes-table tbody");

  try {
    const [newQuotes, allQuotes, projects] = await Promise.all([
      api.get("/admin/quote-requests?status=NEW&size=1"),
      api.get("/admin/quote-requests?size=10"),
      api.get("/admin/projects?size=1"),
    ]);

    statCards.innerHTML = `
      <div class="stat-card"><div class="value">${newQuotes.totalElements}</div><div class="label">New Quote Requests</div></div>
      <div class="stat-card"><div class="value">${allQuotes.totalElements}</div><div class="label">Total Quote Requests</div></div>
      <div class="stat-card"><div class="value">${projects.totalElements}</div><div class="label">Total Projects</div></div>
    `;

    if (!allQuotes.content.length) {
      tbody.innerHTML = `<tr><td colspan="4" class="empty-state">No quote requests yet.</td></tr>`;
    } else {
      tbody.innerHTML = allQuotes.content.map((q) => `
        <tr>
          <td>${escapeHtmlBasic(q.name)}</td>
          <td>${q.category}</td>
          <td>${statusPillHtml(q.status)}</td>
          <td>${new Date(q.createdAt).toLocaleString()}</td>
        </tr>
      `).join("");
    }
  } catch (e) {
    handleAdminApiError(e, statCards);
  }
}

function escapeHtmlBasic(str) {
  const div = document.createElement("div");
  div.textContent = str == null ? "" : String(str);
  return div.innerHTML;
}

document.addEventListener("DOMContentLoaded", loadDashboard);
