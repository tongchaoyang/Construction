const Auth = {
  getToken() {
    return localStorage.getItem("lm_token");
  },
  getUser() {
    const raw = localStorage.getItem("lm_user");
    return raw ? JSON.parse(raw) : null;
  },
  isLoggedIn() {
    return !!this.getToken();
  },
  isAdmin() {
    const user = this.getUser();
    return !!user && user.role === "ADMIN";
  },
  setSession(authResponse) {
    localStorage.setItem("lm_token", authResponse.token);
    localStorage.setItem("lm_user", JSON.stringify(authResponse.user));
  },
  logout() {
    localStorage.removeItem("lm_token");
    localStorage.removeItem("lm_user");
  },
  async login(email, password) {
    const res = await api.post("/auth/login", { email, password });
    this.setSession(res);
    return res;
  },
  async register(email, password, fullName, phone) {
    const res = await api.post("/auth/register", { email, password, fullName, phone });
    this.setSession(res);
    return res;
  },
  requireAdminOrRedirect() {
    if (!this.isLoggedIn() || !this.isAdmin()) {
      window.location.href = "/admin/admin-login.html";
      return false;
    }
    return true;
  },
};
