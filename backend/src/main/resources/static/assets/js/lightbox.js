function ensureLightbox() {
  let overlay = document.getElementById("lightbox-overlay");
  if (overlay) return overlay;

  overlay = document.createElement("div");
  overlay.id = "lightbox-overlay";
  overlay.className = "lightbox-overlay";
  overlay.innerHTML = `
    <button class="lightbox-close" aria-label="Close" type="button">&times;</button>
    <img class="lightbox-image" src="" alt="">
  `;
  document.body.appendChild(overlay);

  overlay.addEventListener("click", (e) => {
    if (e.target === overlay || e.target.classList.contains("lightbox-close")) {
      closeLightbox();
    }
  });
  document.addEventListener("keydown", (e) => {
    if (e.key === "Escape") closeLightbox();
  });

  return overlay;
}

function openLightbox(src, alt) {
  const overlay = ensureLightbox();
  const img = overlay.querySelector(".lightbox-image");
  img.src = src;
  img.alt = alt || "";
  overlay.classList.add("active");
  document.body.style.overflow = "hidden";
}

function closeLightbox() {
  const overlay = document.getElementById("lightbox-overlay");
  if (!overlay) return;
  overlay.classList.remove("active");
  document.body.style.overflow = "";
}

function makeImagesClickable(selector) {
  document.querySelectorAll(selector).forEach((img) => {
    img.addEventListener("click", () => openLightbox(img.currentSrc || img.src, img.alt));
  });
}
