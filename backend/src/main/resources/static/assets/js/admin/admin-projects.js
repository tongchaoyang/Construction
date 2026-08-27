const AdminProjects = {
  list: [],
  currentImageProjectId: null,
};

async function loadProjectsTable() {
  const tbody = document.getElementById("projects-tbody");
  try {
    const res = await api.get("/admin/projects?size=100");
    AdminProjects.list = res.content;
    if (!res.content.length) {
      tbody.innerHTML = `<tr><td colspan="6" class="empty-state">No projects yet. Click "New Project" to add one.</td></tr>`;
      return;
    }
    tbody.innerHTML = res.content.map((p) => `
      <tr>
        <td><img src="${p.coverImageUrl || "/assets/images/logo.png"}" style="width:60px;height:45px;object-fit:cover;border-radius:4px;"></td>
        <td>${escapeHtmlBasic(p.titleEn)}</td>
        <td>${p.category}</td>
        <td>${p.published ? '<span class="status-pill COMPLETED">Published</span>' : '<span class="status-pill CLOSED">Draft</span>'}</td>
        <td>${p.reviewCount > 0 ? p.averageRating.toFixed(1) + " (" + p.reviewCount + ")" : "—"}</td>
        <td>
          <button class="btn btn-sm btn-secondary" data-action="edit" data-id="${p.id}">Edit</button>
          <button class="btn btn-sm btn-secondary" data-action="images" data-id="${p.id}">Photos</button>
          <button class="btn btn-sm btn-secondary" data-action="reviews" data-id="${p.id}">Reviews</button>
          <button class="btn btn-sm btn-danger" data-action="delete" data-id="${p.id}">Delete</button>
        </td>
      </tr>
    `).join("");

    tbody.querySelectorAll("button[data-action]").forEach((btn) => {
      const id = parseInt(btn.getAttribute("data-id"), 10);
      const action = btn.getAttribute("data-action");
      btn.addEventListener("click", () => {
        if (action === "edit") openProjectModal(id);
        if (action === "images") openImagesModal(id);
        if (action === "reviews") openReviewsModal(id);
        if (action === "delete") deleteProject(id);
      });
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

// ---- Project create/edit modal ----

function openProjectModal(id) {
  document.getElementById("project-form-error").innerHTML = "";
  document.getElementById("project-form").reset();
  document.getElementById("project-id").value = id || "";
  document.getElementById("project-modal-title").textContent = id ? "Edit Project" : "New Project";

  if (id) {
    const p = AdminProjects.list.find((x) => x.id === id);
    api.get("/projects/" + id).then((detail) => {
      document.getElementById("titleEn").value = detail.titleEn;
      document.getElementById("titleZh").value = detail.titleZh;
      document.getElementById("descriptionEn").value = detail.descriptionEn;
      document.getElementById("descriptionZh").value = detail.descriptionZh;
      document.getElementById("category").value = detail.category;
      document.getElementById("location").value = detail.location || "";
      document.getElementById("completedDate").value = detail.completedDate || "";
      document.getElementById("published").checked = detail.published;
    }).catch((e) => handleAdminApiError(e, document.getElementById("project-form-error")));
  } else {
    document.getElementById("published").checked = true;
  }

  document.getElementById("project-modal-overlay").style.display = "flex";
}

function closeProjectModal() {
  document.getElementById("project-modal-overlay").style.display = "none";
}

async function saveProject() {
  const errorBox = document.getElementById("project-form-error");
  errorBox.innerHTML = "";
  const id = document.getElementById("project-id").value;
  const body = {
    titleEn: document.getElementById("titleEn").value.trim(),
    titleZh: document.getElementById("titleZh").value.trim(),
    descriptionEn: document.getElementById("descriptionEn").value.trim(),
    descriptionZh: document.getElementById("descriptionZh").value.trim(),
    category: document.getElementById("category").value,
    location: document.getElementById("location").value.trim(),
    completedDate: document.getElementById("completedDate").value || null,
    published: document.getElementById("published").checked,
  };
  try {
    if (id) {
      await api.put("/admin/projects/" + id, body);
    } else {
      await api.post("/admin/projects", body);
    }
    closeProjectModal();
    await loadProjectsTable();
  } catch (e) {
    handleAdminApiError(e, errorBox);
  }
}

async function deleteProject(id) {
  if (!confirm("Delete this project? This cannot be undone.")) return;
  try {
    await api.del("/admin/projects/" + id);
    await loadProjectsTable();
  } catch (e) {
    if (isAuthExpired(e)) return;
    alert(e.message);
  }
}

// ---- Image management modal ----

async function openImagesModal(id) {
  AdminProjects.currentImageProjectId = id;
  document.getElementById("images-error").innerHTML = "";
  document.getElementById("new-images-input").value = "";
  await renderImageManager();
  document.getElementById("images-modal-overlay").style.display = "flex";
}

function closeImagesModal() {
  document.getElementById("images-modal-overlay").style.display = "none";
}

async function renderImageManager() {
  const manager = document.getElementById("image-manager");
  let detail;
  try {
    detail = await api.get("/projects/" + AdminProjects.currentImageProjectId);
  } catch (e) {
    handleAdminApiError(e, manager);
    return;
  }
  if (!detail.images.length) {
    manager.innerHTML = `<div class="empty-state">No photos yet.</div>`;
    return;
  }
  manager.innerHTML = detail.images.map((img, idx) => `
    <div class="img-item" data-id="${img.id}">
      <img src="${img.imageUrl}">
      <button data-action="delete-image" data-id="${img.id}" title="Delete">&times;</button>
      <div style="display:flex;justify-content:space-between;margin-top:4px;">
        <button class="btn btn-sm btn-secondary" data-action="move-left" data-idx="${idx}" ${idx === 0 ? "disabled" : ""}>&#8592;</button>
        <button class="btn btn-sm btn-secondary" data-action="move-right" data-idx="${idx}" ${idx === detail.images.length - 1 ? "disabled" : ""}>&#8594;</button>
      </div>
    </div>
  `).join("");

  manager.querySelectorAll('button[data-action="delete-image"]').forEach((btn) => {
    btn.addEventListener("click", async () => {
      if (!confirm("Delete this photo?")) return;
      try {
        await api.del(`/admin/projects/${AdminProjects.currentImageProjectId}/images/${btn.getAttribute("data-id")}`);
        await renderImageManager();
        await loadProjectsTable();
      } catch (e) {
        handleAdminApiError(e, document.getElementById("images-error"));
      }
    });
  });

  const ids = detail.images.map((i) => i.id);
  manager.querySelectorAll('button[data-action="move-left"]').forEach((btn) => {
    btn.addEventListener("click", () => reorderImage(ids, parseInt(btn.getAttribute("data-idx"), 10), -1));
  });
  manager.querySelectorAll('button[data-action="move-right"]').forEach((btn) => {
    btn.addEventListener("click", () => reorderImage(ids, parseInt(btn.getAttribute("data-idx"), 10), 1));
  });
}

async function reorderImage(ids, index, direction) {
  const newIds = ids.slice();
  const target = index + direction;
  if (target < 0 || target >= newIds.length) return;
  [newIds[index], newIds[target]] = [newIds[target], newIds[index]];
  try {
    await api.put(`/admin/projects/${AdminProjects.currentImageProjectId}/images/reorder`, { imageIds: newIds });
    await renderImageManager();
    await loadProjectsTable();
  } catch (e) {
    handleAdminApiError(e, document.getElementById("images-error"));
  }
}

async function uploadNewImages() {
  const input = document.getElementById("new-images-input");
  const errorBox = document.getElementById("images-error");
  errorBox.innerHTML = "";
  if (!input.files.length) return;
  const formData = new FormData();
  for (let i = 0; i < input.files.length; i++) {
    formData.append("files", input.files[i]);
  }
  try {
    await api.post(`/admin/projects/${AdminProjects.currentImageProjectId}/images`, formData);
    input.value = "";
    await renderImageManager();
    await loadProjectsTable();
  } catch (e) {
    handleAdminApiError(e, errorBox);
  }
}

// ---- Review moderation modal ----

async function openReviewsModal(id) {
  AdminProjects.currentImageProjectId = id;
  document.getElementById("reviews-error").innerHTML = "";
  await renderReviewsManager();
  document.getElementById("reviews-modal-overlay").style.display = "flex";
}

async function renderReviewsManager() {
  const container = document.getElementById("reviews-manager");
  let res;
  try {
    res = await api.get(`/projects/${AdminProjects.currentImageProjectId}/reviews?size=100`);
  } catch (e) {
    handleAdminApiError(e, container);
    return;
  }
  if (!res.content.length) {
    container.innerHTML = `<div class="empty-state">No reviews yet.</div>`;
    return;
  }
  container.innerHTML = res.content.map((r) => `
    <div class="review-item">
      <div class="review-head">
        <span class="reviewer">${escapeHtmlBasic(r.userFullName)} — ${"★".repeat(r.rating)}${"☆".repeat(5 - r.rating)}</span>
        <button class="btn btn-sm btn-danger" data-review-id="${r.id}">Delete</button>
      </div>
      ${r.comment ? `<p style="margin-bottom:0;">${escapeHtmlBasic(r.comment)}</p>` : ""}
    </div>
  `).join("");

  container.querySelectorAll("button[data-review-id]").forEach((btn) => {
    btn.addEventListener("click", async () => {
      if (!confirm("Delete this review?")) return;
      try {
        await api.del("/admin/reviews/" + btn.getAttribute("data-review-id"));
        await renderReviewsManager();
        await loadProjectsTable();
      } catch (e) {
        handleAdminApiError(e, document.getElementById("reviews-error"));
      }
    });
  });
}

document.addEventListener("DOMContentLoaded", () => {
  if (!renderAdminShell("projects")) return;
  loadProjectsTable();

  document.getElementById("new-project-btn").addEventListener("click", () => openProjectModal(null));
  document.getElementById("project-modal-cancel").addEventListener("click", closeProjectModal);
  document.getElementById("project-modal-save").addEventListener("click", saveProject);
  document.getElementById("images-modal-close").addEventListener("click", closeImagesModal);
  document.getElementById("upload-images-btn").addEventListener("click", uploadNewImages);
  document.getElementById("reviews-modal-close").addEventListener("click", () => {
    document.getElementById("reviews-modal-overlay").style.display = "none";
  });
});
