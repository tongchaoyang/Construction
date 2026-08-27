const I18n = (() => {
  let dict = {};
  let lang = localStorage.getItem("lm_lang") || "en";

  function lookup(key) {
    return key.split(".").reduce((acc, part) => (acc && acc[part] !== undefined ? acc[part] : undefined), dict);
  }

  function t(key, fallback) {
    const value = lookup(key);
    return value !== undefined ? value : (fallback !== undefined ? fallback : key);
  }

  function apply(root = document) {
    root.querySelectorAll("[data-i18n]").forEach((el) => {
      const key = el.getAttribute("data-i18n");
      const value = lookup(key);
      if (value !== undefined) {
        el.textContent = value;
      }
    });
    root.querySelectorAll("[data-i18n-placeholder]").forEach((el) => {
      const key = el.getAttribute("data-i18n-placeholder");
      const value = lookup(key);
      if (value !== undefined) {
        el.setAttribute("placeholder", value);
      }
    });
    document.documentElement.lang = lang === "zh" ? "zh-CN" : "en";
    document.querySelectorAll("[data-lang-label]").forEach((el) => {
      el.textContent = lang === "zh" ? "EN" : "中文";
    });
  }

  async function loadDictionary(targetLang) {
    const res = await fetch("/assets/i18n/" + targetLang + ".json");
    dict = await res.json();
  }

  async function init() {
    await loadDictionary(lang);
    apply();
    document.dispatchEvent(new CustomEvent("i18n:ready"));
  }

  async function setLanguage(newLang) {
    lang = newLang;
    localStorage.setItem("lm_lang", newLang);
    await loadDictionary(newLang);
    apply();
    document.dispatchEvent(new CustomEvent("i18n:changed", { detail: { lang } }));
  }

  function current() {
    return lang;
  }

  function pick(enValue, zhValue) {
    return lang === "zh" ? (zhValue || enValue) : (enValue || zhValue);
  }

  return { t, apply, init, setLanguage, current, pick };
})();

document.addEventListener("DOMContentLoaded", () => {
  I18n.init();
});
