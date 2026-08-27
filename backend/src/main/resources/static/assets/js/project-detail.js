const DetailPage = {
  projectId: new URLSearchParams(window.location.search).get("id"),
  project: null,
  reviews: [],
  selectedRating: 0,
};

async function loadProjectDetail() {
  const container = document.getElementById("project-container");
  if (!DetailPage.projectId) {
    container.innerHTML = `<div class="alert alert-error" data-i18n="detail.missingId">No project specified.</div>`;
    return;
  }
  try {
    const [project, reviewsPage] = await Promise.all([
      api.get("/projects/" + DetailPage.projectId),
      api.get("/projects/" + DetailPage.projectId + "/reviews?size=50"),
    ]);
    DetailPage.project = project;
    DetailPage.reviews = reviewsPage.content;
    renderDetail();
  } catch (e) {
    renderErrorAlert(container, e);
  }
}

function renderDetail() {
  const p = DetailPage.project;
  const container = document.getElementById("project-container");
  const title = I18n.pick(p.titleEn, p.titleZh);
  const description = I18n.pick(p.descriptionEn, p.descriptionZh);
  const images = p.images.length ? p.images : (p.coverImageUrl ? [{ imageUrl: p.coverImageUrl }] : []);
  const mainImg = images[0] ? images[0].imageUrl : "/assets/images/logo.png";

  container.innerHTML = `
    <div class="detail-header">
      <span class="badge category-${p.category}">${categoryLabel(p.category)}</span>
      <h1>${escapeHtml(title)}</h1>
      <div class="detail-meta">
        ${escapeHtml(p.location || "")}${p.completedDate ? " · " + formatDate(p.completedDate) : ""}
        ${p.reviewCount > 0 ? ` · <span class="stars">${starString(p.averageRating)}</span> ${p.averageRating.toFixed(1)} (${p.reviewCount})` : ""}
      </div>
    </div>

    <div class="detail-gallery">
      <div class="main-image" id="main-image" style="background-image:url('${mainImg}')"></div>
      <div class="thumb-strip" id="thumb-strip">
        ${images.map((img, i) => `<img src="${img.imageUrl}" class="${i === 0 ? "active" : ""}" data-url="${img.imageUrl}">`).join("")}
      </div>
    </div>

    <div class="detail-description">${escapeHtml(description)}</div>

    <h2 data-i18n="detail.reviewsTitle">Customer Reviews</h2>
    <div id="review-form-area"></div>
    <div id="review-list"></div>
  `;

  document.querySelectorAll("#thumb-strip img").forEach((thumb) => {
    thumb.addEventListener("click", () => {
      document.getElementById("main-image").style.backgroundImage = `url('${thumb.getAttribute("data-url")}')`;
      document.querySelectorAll("#thumb-strip img").forEach((t) => t.classList.remove("active"));
      thumb.classList.add("active");
    });
  });

  renderReviewForm();
  renderReviewList();
  I18n.apply(container);
}

function renderReviewList() {
  const list = document.getElementById("review-list");
  if (!DetailPage.reviews.length) {
    list.innerHTML = `<div class="empty-state" data-i18n="detail.noReviews">Be the first to review this project.</div>`;
    return;
  }
  list.innerHTML = DetailPage.reviews.map((r) => `
    <div class="review-item">
      <div class="review-head">
        <span class="reviewer">${escapeHtml(r.userFullName)}</span>
        <span class="review-date">${new Date(r.createdAt).toLocaleDateString()}</span>
      </div>
      <div class="stars">${starString(r.rating)}</div>
      ${r.comment ? `<p style="margin-bottom:0;">${escapeHtml(r.comment)}</p>` : ""}
    </div>
  `).join("");
}

function renderReviewForm() {
  const area = document.getElementById("review-form-area");
  if (!Auth.isLoggedIn()) {
    area.innerHTML = `
      <div class="alert" style="background:#eef2f7;color:var(--color-navy);">
        <span data-i18n="detail.loginToReview">Please log in to leave a rating and comment.</span>
        <a href="/login.html?redirect=${encodeURIComponent(window.location.href)}" data-i18n="nav.login">Log in</a>
      </div>
    `;
    I18n.apply(area);
    return;
  }

  const user = Auth.getUser();
  const existing = DetailPage.reviews.find((r) => r.userId === user.id);
  DetailPage.selectedRating = existing ? existing.rating : 0;

  area.innerHTML = `
    <div class="form-card" style="margin:0 0 24px;max-width:none;">
      <h3 style="margin-top:0;" data-i18n="${existing ? "detail.editReview" : "detail.addReview"}">${existing ? "Edit Your Review" : "Leave a Review"}</h3>
      <div id="review-error"></div>
      <div class="star-picker" id="star-picker">
        ${[1, 2, 3, 4, 5].map((n) => `<span data-value="${n}">★</span>`).join("")}
      </div>
      <div class="form-group">
        <textarea id="review-comment" data-i18n-placeholder="detail.commentPlaceholder" placeholder="Share your experience (optional)">${existing && existing.comment ? escapeHtml(existing.comment) : ""}</textarea>
      </div>
      <div style="display:flex;gap:10px;">
        <button class="btn btn-primary" id="submit-review-btn" data-i18n="${existing ? "detail.updateReview" : "detail.submitReview"}">${existing ? "Update Review" : "Submit Review"}</button>
        ${existing ? `<button class="btn btn-secondary" id="delete-review-btn" data-i18n="detail.deleteReview">Delete My Review</button>` : ""}
      </div>
    </div>
  `;

  paintStars();
  document.querySelectorAll("#star-picker span").forEach((star) => {
    star.addEventListener("click", () => {
      DetailPage.selectedRating = parseInt(star.getAttribute("data-value"), 10);
      paintStars();
    });
  });

  document.getElementById("submit-review-btn").addEventListener("click", () => submitReview(!!existing));
  const deleteBtn = document.getElementById("delete-review-btn");
  if (deleteBtn) {
    deleteBtn.addEventListener("click", deleteOwnReview);
  }
  I18n.apply(area);
}

function paintStars() {
  document.querySelectorAll("#star-picker span").forEach((star) => {
    const value = parseInt(star.getAttribute("data-value"), 10);
    star.classList.toggle("selected", value <= DetailPage.selectedRating);
  });
}

async function submitReview(isUpdate) {
  const errorBox = document.getElementById("review-error");
  errorBox.innerHTML = "";
  if (!DetailPage.selectedRating) {
    errorBox.innerHTML = `<div class="alert alert-error" data-i18n="detail.selectRating">Please select a star rating.</div>`;
    I18n.apply(errorBox);
    return;
  }
  const comment = document.getElementById("review-comment").value.trim();
  try {
    const body = { rating: DetailPage.selectedRating, comment: comment || null };
    if (isUpdate) {
      await api.put(`/projects/${DetailPage.projectId}/reviews/me`, body);
    } else {
      await api.post(`/projects/${DetailPage.projectId}/reviews`, body);
    }
    await loadProjectDetail();
  } catch (e) {
    errorBox.innerHTML = `<div class="alert alert-error">${escapeHtml(e.message)}</div>`;
  }
}

async function deleteOwnReview() {
  if (!confirm(I18n.t("detail.confirmDelete", "Delete your review?"))) return;
  try {
    await api.del(`/projects/${DetailPage.projectId}/reviews/me`);
    await loadProjectDetail();
  } catch (e) {
    alert(e.message);
  }
}

document.addEventListener("DOMContentLoaded", loadProjectDetail);
document.addEventListener("i18n:changed", () => {
  if (DetailPage.project) renderDetail();
});
