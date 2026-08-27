const AdminQuotes = {
  statusFilter: "",
  currentId: null,
};

async function loadQuotesTable() {
  const tbody = document.getElementById("quotes-tbody");
  try {
    const params = new URLSearchParams({ size: 100 });
    if (AdminQuotes.statusFilter) params.set("status", AdminQuotes.statusFilter);
    const res = await api.get("/admin/quote-requests?" + params.toString());
    if (!res.content.length) {
      tbody.innerHTML = `<tr><td colspan="6" class="empty-state">No quote requests found.</td></tr>`;
      return;
    }
    tbody.innerHTML = res.content.map((q) => `
      <tr>
        <td>${escapeHtmlBasic(q.name)}</td>
        <td>${escapeHtmlBasic(q.email)}<br>${escapeHtmlBasic(q.phone)}</td>
        <td>${q.category}</td>
        <td>${statusPillHtml(q.status)}</td>
        <td>${new Date(q.createdAt).toLocaleString()}</td>
        <td><button class="btn btn-sm btn-secondary" data-id="${q.id}">View</button></td>
      </tr>
    `).join("");

    tbody.querySelectorAll("button[data-id]").forEach((btn) => {
      btn.addEventListener("click", () => openQuoteModal(parseInt(btn.getAttribute("data-id"), 10)));
    });
  } catch (e) {
    handleAdminApiError(e, document.getElementById("list-error"));
  }
}

function escapeHtmlBasic(str) {
  const div = document.createElement("div");
  div.textContent = str == null ? "" : String(str);
  return div.innerHTML;
}

async function openQuoteModal(id) {
  AdminQuotes.currentId = id;
  const body = document.getElementById("quote-detail-body");
  body.innerHTML = "<p>Loading...</p>";
  document.getElementById("quote-modal-overlay").style.display = "flex";
  try {
    const q = await api.get("/admin/quote-requests/" + id);
    body.innerHTML = `
      <p><strong>Name:</strong> ${escapeHtmlBasic(q.name)}</p>
      <p><strong>Email:</strong> ${escapeHtmlBasic(q.email)} &nbsp; <strong>Phone:</strong> ${escapeHtmlBasic(q.phone)}</p>
      <p><strong>Category:</strong> ${q.category}</p>
      <p><strong>Description:</strong><br>${escapeHtmlBasic(q.description)}</p>
      ${q.imageUrls.length ? `<div class="file-preview-grid">${q.imageUrls.map((u) => `<img src="${u}">`).join("")}</div>` : "<p><em>No photos attached.</em></p>"}
      <div class="form-group" style="margin-top:16px;">
        <label>Status</label>
        <select id="quote-status-select">
          ${["NEW", "CONTACTED", "IN_PROGRESS", "COMPLETED", "CLOSED"].map((s) => `<option value="${s}" ${s === q.status ? "selected" : ""}>${s.replace("_", " ")}</option>`).join("")}
        </select>
      </div>
      <div class="form-group">
        <label>Admin Notes</label>
        <textarea id="quote-notes-input">${escapeHtmlBasic(q.adminNotes || "")}</textarea>
      </div>
      <div id="quote-save-error"></div>
    `;
  } catch (e) {
    handleAdminApiError(e, body);
  }
}

async function saveQuoteStatus() {
  const status = document.getElementById("quote-status-select").value;
  const adminNotes = document.getElementById("quote-notes-input").value;
  try {
    await api.patch(`/admin/quote-requests/${AdminQuotes.currentId}/status`, { status, adminNotes });
    document.getElementById("quote-modal-overlay").style.display = "none";
    await loadQuotesTable();
  } catch (e) {
    handleAdminApiError(e, document.getElementById("quote-save-error"));
  }
}

document.addEventListener("DOMContentLoaded", () => {
  if (!renderAdminShell("quotes")) return;
  loadQuotesTable();

  document.querySelectorAll("#status-filter button").forEach((btn) => {
    btn.addEventListener("click", () => {
      document.querySelectorAll("#status-filter button").forEach((b) => b.classList.remove("active"));
      btn.classList.add("active");
      AdminQuotes.statusFilter = btn.getAttribute("data-status");
      loadQuotesTable();
    });
  });

  document.getElementById("quote-modal-close").addEventListener("click", () => {
    document.getElementById("quote-modal-overlay").style.display = "none";
  });
  document.getElementById("quote-modal-save").addEventListener("click", saveQuoteStatus);
});
