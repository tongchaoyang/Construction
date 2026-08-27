const ProjectsPage = {
  category: "",
  page: 0,
  size: 9,
};

async function loadProjects() {
  const grid = document.getElementById("project-grid");
  const pagination = document.getElementById("pagination");
  const params = new URLSearchParams({ page: ProjectsPage.page, size: ProjectsPage.size });
  if (ProjectsPage.category) params.set("category", ProjectsPage.category);

  try {
    const res = await api.get("/projects?" + params.toString());
    if (!res.content.length) {
      grid.innerHTML = `<div class="empty-state" data-i18n="projects.empty">No projects published yet. Check back soon!</div>`;
      I18n.apply(grid);
      pagination.innerHTML = "";
      return;
    }
    grid.innerHTML = res.content.map(projectCardHtml).join("");
    renderPagination(pagination, res.page, res.totalPages);
  } catch (e) {
    renderErrorAlert(grid, e);
  }
}

function renderPagination(container, current, totalPages) {
  if (totalPages <= 1) {
    container.innerHTML = "";
    return;
  }
  let html = "";
  for (let i = 0; i < totalPages; i++) {
    html += `<button data-page="${i}" class="${i === current ? "active" : ""}">${i + 1}</button>`;
  }
  container.innerHTML = html;
  container.querySelectorAll("button").forEach((btn) => {
    btn.addEventListener("click", () => {
      ProjectsPage.page = parseInt(btn.getAttribute("data-page"), 10);
      loadProjects();
      window.scrollTo({ top: 0, behavior: "smooth" });
    });
  });
}

document.addEventListener("DOMContentLoaded", () => {
  loadProjects();
  document.querySelectorAll("#category-filter button").forEach((btn) => {
    btn.addEventListener("click", () => {
      document.querySelectorAll("#category-filter button").forEach((b) => b.classList.remove("active"));
      btn.classList.add("active");
      ProjectsPage.category = btn.getAttribute("data-category");
      ProjectsPage.page = 0;
      loadProjects();
    });
  });
});
document.addEventListener("i18n:changed", loadProjects);
