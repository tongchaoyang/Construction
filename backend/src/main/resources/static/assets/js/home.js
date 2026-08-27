async function loadFeaturedProjects() {
  const container = document.getElementById("featured-projects");
  try {
    const res = await api.get("/projects?size=3");
    if (!res.content.length) {
      container.innerHTML = `<div class="empty-state" data-i18n="projects.empty">No projects published yet. Check back soon!</div>`;
      return;
    }
    container.innerHTML = res.content.map(projectCardHtml).join("");
  } catch (e) {
    renderErrorAlert(container, e);
  }
}

document.addEventListener("DOMContentLoaded", loadFeaturedProjects);
document.addEventListener("i18n:changed", loadFeaturedProjects);
