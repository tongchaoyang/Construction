function starString(rating) {
  const full = Math.round(rating);
  return "★".repeat(full) + "☆".repeat(5 - full);
}

function categoryLabel(category) {
  const key = "category." + category;
  return I18n.t(key, category);
}

function formatDate(dateStr) {
  if (!dateStr) return "";
  const d = new Date(dateStr + "T00:00:00");
  if (isNaN(d.getTime())) return dateStr;
  const lang = I18n.current();
  return d.toLocaleDateString(lang === "zh" ? "zh-CN" : "en-US", { year: "numeric", month: "long" });
}

function projectCardHtml(project) {
  const title = I18n.pick(project.titleEn, project.titleZh);
  const img = project.coverImageUrl || "/assets/images/logo.png";
  const ratingLine = project.reviewCount > 0
    ? `<span class="stars">${starString(project.averageRating)}</span> ${project.averageRating.toFixed(1)} (${project.reviewCount})`
    : I18n.t("projects.noReviews", "No reviews yet");

  return `
    <a href="/project-detail.html?id=${project.id}" class="project-card" style="text-decoration:none;color:inherit;">
      <div class="thumb" style="background-image:url('${img}')"></div>
      <div class="body">
        <span class="badge category-${project.category}">${categoryLabel(project.category)}</span>
        <h3>${escapeHtml(title)}</h3>
        <div class="meta">${escapeHtml(project.location || "")}${project.completedDate ? " · " + formatDate(project.completedDate) : ""}</div>
        <div class="rating-line">${ratingLine}</div>
      </div>
    </a>
  `;
}

function renderErrorAlert(container, error) {
  container.innerHTML = `<div class="alert alert-error">${escapeHtml(error.message || String(error))}</div>`;
}
