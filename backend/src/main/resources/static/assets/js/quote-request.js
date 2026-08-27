function prefillFromAccount() {
  if (!Auth.isLoggedIn()) return;
  const user = Auth.getUser();
  document.getElementById("name").value = user.fullName || "";
  document.getElementById("email").value = user.email || "";
  document.getElementById("phone").value = user.phone || "";
}

function renderFilePreview() {
  const input = document.getElementById("files");
  const preview = document.getElementById("file-preview");
  preview.innerHTML = "";
  if (!input.files.length) return;
  Array.from(input.files).forEach((file) => {
    const url = URL.createObjectURL(file);
    const img = document.createElement("img");
    img.src = url;
    preview.appendChild(img);
  });
}

async function handleQuoteSubmit(e) {
  e.preventDefault();
  const errorBox = document.getElementById("form-error");
  const successBox = document.getElementById("form-success");
  const submitBtn = document.getElementById("submit-btn");
  errorBox.innerHTML = "";
  successBox.innerHTML = "";

  const formData = new FormData();
  formData.append("name", document.getElementById("name").value.trim());
  formData.append("email", document.getElementById("email").value.trim());
  formData.append("phone", document.getElementById("phone").value.trim());
  formData.append("category", document.getElementById("category").value);
  formData.append("description", document.getElementById("description").value.trim());
  const files = document.getElementById("files").files;
  for (let i = 0; i < files.length; i++) {
    formData.append("files", files[i]);
  }

  submitBtn.disabled = true;
  try {
    await api.post("/quote-requests", formData);
    successBox.innerHTML = `<div class="alert alert-success" data-i18n="quote.successMessage">Thank you! Your request has been submitted. Our team will contact you shortly.</div>`;
    I18n.apply(successBox);
    document.getElementById("quote-form").reset();
    document.getElementById("file-preview").innerHTML = "";
    window.scrollTo({ top: 0, behavior: "smooth" });
  } catch (err) {
    errorBox.innerHTML = `<div class="alert alert-error">${err.message}</div>`;
  } finally {
    submitBtn.disabled = false;
  }
}

document.addEventListener("DOMContentLoaded", () => {
  prefillFromAccount();
  document.getElementById("files").addEventListener("change", renderFilePreview);
  document.getElementById("quote-form").addEventListener("submit", handleQuoteSubmit);
});
