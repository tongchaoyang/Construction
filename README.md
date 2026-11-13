# Landmark Construction & ADU — Website

Bilingual (English / 中文) marketing website for Landmark Construction & ADU, a San Jose residential construction and ADU contractor (CSLB #1140588). Built with Spring Boot (Java) on the backend and a plain HTML/CSS/JavaScript frontend served as static resources from the same application.

## Features

- Bilingual marketing pages (home, about, services) with a client-side language toggle (English / 中文)
- Public gallery of completed projects, filterable by category, with photo galleries
- Customer accounts (register/login) to rate and comment on projects (one review per user per project)
- Open "Request a Quote" form (no account required) with multi-photo upload of the customer's lot/house/rooms
- Email notifications (business inbox + customer confirmation) on new quote requests
- Admin portal to manage projects (with photo upload/reorder), quote requests (status tracking), and moderate reviews
- Images stored in S3-compatible object storage (works with RustFS locally, AWS S3 / Cloudflare R2 in production)
- Single Sign-On via Google and/or any generic OIDC provider (Okta, Auth0, Keycloak, etc.), in addition to email/password — both customers and admins can use it, and logging in with the same email as an existing password account auto-links to it

## Requirements

- Java 17
- Maven
- Docker (for local Postgres + RustFS)

## Local Development

1. Start the local database and object storage:

   ```bash
   docker compose up -d
   ```

   This starts Postgres on `5432` and RustFS (S3-compatible storage) on `9000` (API) / `9001` (console), and creates the `landmark-photos` bucket automatically.

2. Set the initial admin account (only needed the first time — the app creates this admin user on startup if no admin exists yet):

   ```bash
   export ADMIN_EMAIL=admin@yourdomain.com
   export ADMIN_PASSWORD=ChangeMe123!
   ```

3. Run the backend (serves both the API and the frontend at `http://localhost:8080`):

   ```bash
   cd backend
   mvn spring-boot:run
   ```

4. Open `http://localhost:8080` in your browser.

Local email sending is not configured by default — quote-request emails will simply be logged as a warning rather than failing the request. To test real email delivery locally, set `SMTP_HOST`, `SMTP_PORT`, `SMTP_USERNAME`, `SMTP_APP_PASSWORD` env vars (see below).

## Configuration (environment variables)

| Variable | Purpose | Default (local profile) |
|---|---|---|
| `ADMIN_EMAIL` / `ADMIN_PASSWORD` | Bootstraps the first admin account on startup (only if none exists) | — |
| `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` | Postgres connection (prod profile) | local: `jdbc:postgresql://localhost:5432/landmark` |
| `S3_ENDPOINT` / `S3_REGION` / `S3_BUCKET` / `S3_ACCESS_KEY` / `S3_SECRET_KEY` / `S3_PATH_STYLE_ACCESS` / `S3_PUBLIC_BASE_URL` | S3-compatible object storage config | local: RustFS on `localhost:9000` |
| `SMTP_HOST` / `SMTP_PORT` / `SMTP_USERNAME` / `SMTP_APP_PASSWORD` | SMTP for quote-request notification/confirmation emails (e.g. a Gmail App Password) | none (emails logged, not sent) |
| `BUSINESS_NOTIFICATION_EMAIL` | Inbox that receives new quote-request notifications | `landmarkadu@gmail.com` |
| `JWT_SECRET` / `JWT_EXPIRATION_MS` | JWT signing secret and token lifetime | dev-only default secret — **must be overridden in production** |
| `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` | Enables "Sign in with Google" SSO when both are set | unset (Google SSO disabled) |
| `OIDC_ISSUER_URI` / `OIDC_CLIENT_ID` / `OIDC_CLIENT_SECRET` / `OIDC_DISPLAY_NAME` | Enables SSO against any generic OIDC provider when all three are set; `OIDC_DISPLAY_NAME` controls the button label (e.g. "Sign in with Okta") | unset (generic OIDC SSO disabled) |
| `SPRING_PROFILES_ACTIVE` | `local` (default) or `prod` | `local` |

For production, run with `SPRING_PROFILES_ACTIVE=prod` and provide real values for the database, S3, SMTP, and `JWT_SECRET` variables — do not reuse the local development defaults.

## Setting up Single Sign-On

SSO is entirely optional and off by default — the site works fully with email/password if you never set these variables. Both Google and the generic OIDC option can be enabled independently, and the login/register/admin-login pages automatically show or hide each "Sign in with..." button based on what's configured (checked via `GET /api/auth/sso-providers`), so a misconfigured or unreachable provider never breaks the site — it just disables that one button and logs a warning on startup.

**Google:**
1. In [Google Cloud Console](https://console.cloud.google.com/), create (or reuse) a project, then go to "APIs & Services" → "Credentials" → "Create Credentials" → "OAuth client ID" (type: Web application).
2. Add an **Authorized redirect URI** of `http://localhost:8080/login/oauth2/code/google` for local dev (and your real domain's equivalent, e.g. `https://your-domain.com/login/oauth2/code/google`, for production).
3. Set `GOOGLE_CLIENT_ID` and `GOOGLE_CLIENT_SECRET` from the credentials Google gives you, then restart the app.

**Generic OIDC (Okta, Auth0, Keycloak, etc.):**
1. Register a new application/client with your identity provider, using a redirect URI of `http://localhost:8080/login/oauth2/code/oidc` (or your production domain equivalent).
2. Set `OIDC_ISSUER_URI` (the provider's issuer URL, used for OIDC discovery), `OIDC_CLIENT_ID`, `OIDC_CLIENT_SECRET`, and optionally `OIDC_DISPLAY_NAME`, then restart the app.
3. If the issuer URL is unreachable or misconfigured, the app still starts — it logs a warning and disables that provider rather than failing to boot.

**Account linking:** the first time someone signs in via SSO, the app looks for an existing account with the same (provider-verified) email address and links to it automatically; if none exists, it creates a new customer account with no password (they can set one later from the Change Password page). An admin account only ever gets admin access via SSO if that same email was already an admin — SSO logins can never self-provision as admin.

## Project Structure

```
backend/            Spring Boot application (Java 17, Maven)
  src/main/java/     REST API: auth, projects, reviews, quote requests, admin endpoints
  src/main/resources/
    db/migration/     Flyway schema migrations
    static/           Frontend (HTML/CSS/JS), served at the app root
docker-compose.yml   Local Postgres + RustFS (S3) for development
```
