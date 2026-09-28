# Murach Ch14 – How to use JavaMail to send email

This is the **Email List** app from Chapter 14 of *Murach's Java Servlets/JSP (3rd Ed.)*, finished and extended so it can be deployed with Docker on [Render](https://render.com).

- The user fills out the Join form. The app validates the input, saves it to `UserDB` and sends a welcome email with **JavaMail** (text/plain or text/html).
- `MailUtilLocal` sends through an SMTP server on localhost. `MailUtilGmail` sends through a remote SMTPS server (Gmail) that requires authentication.
- The `/admin` page sends one email to the whole list (`setRecipients` + BCC).
- The **`/mail` (Mail Services)** page shows the status of every mail service and lets an admin test a connection, compose an email and view the send history (see below).
- The `/questions.jsp` page contains **full answers to the chapter's questions** (also copied below).

## Improvements over the slides

| Slides | This project |
|---|---|
| Username/password hard-coded (`"sesame"`) | Read from env vars `SMTP_USERNAME` / `SMTP_PASSWORD` |
| `Session.getDefaultInstance` → Tomcat must be restarted after changing config | `Session.getInstance` → a new config takes effect immediately |
| `transport.close()` is skipped when an error occurs | `close()` runs in a `finally` block |
| JAF assumed to ship with the JDK | Java 11+ removed JAF → `javax.activation` added to the pom |
| No input validation | `UserValidator` + duplicate-email check, XSS escaping (`c:out`, `HtmlUtil`) |
| ASCII only | UTF-8 everywhere (Vietnamese is supported) |
| SMTP only | Extra `MAIL_MODE=brevo` / `resend` (HTTPS APIs) because Render free blocks SMTP ports |
| – | CC/BCC, multiple recipients, admin page with login + CSRF token, `/health` |

## Mail Services page (`/mail`)

Open it from the **Mail Services** link in the navigation bar (or the button on the home page).

| Feature | Who can use it | Description |
|---|---|---|
| Service status | Anyone | 5 services: `log`, `local` (MailUtilLocal), `gmail` (MailUtilGmail), `resend`, `brevo` (HTTPS APIs). Each card shows **Default / Configured / Not configured** and its host/port. Credentials are masked (`jo***@gmail.com`). |
| Test connection | Admin | Connects to or logs in to the SMTP server (`transport.connect()`), or checks the Resend/Brevo API key, **without sending an email**. |
| Compose email | Admin | Pick the service to send through, To/CC/BCC (several addresses separated by commas), subject, text/plain or text/html body. Max 50 recipients. |
| Send history | Admin | The last 50 sends (welcome emails, admin broadcasts, composed emails) with status `SENT` / `LOGGED` / `FAILED` and the error message. |

Sending requires logging in with `ADMIN_PASSWORD`, plus a CSRF token, so a public deployment on Render can't be abused as an open relay.

Code: `MailServiceServlet` → `MailService.send(email, mode)` / `testConnection(mode)` / `services()`. History lives in `MailLog`, and the login/CSRF code shared with `/admin` lives in `AdminAuth`.

Local test with Mailpit (a fake SMTP server with a web UI at http://localhost:8025):

```bash
docker network create ch14net
```

```bash
docker run -d --name mailpit --network ch14net -p 8025:8025 axllent/mailpit
```

```bash
docker run --rm --network ch14net -p 8080:8080 -e MAIL_MODE=local -e SMTP_HOST=mailpit -e SMTP_PORT=1025 -e ADMIN_PASSWORD=change-me ch14-email
```

## Configuration (environment variables)

| Variable | Meaning |
|---|---|
| `MAIL_MODE` | `log` (default – no sending, only logs and a preview), `local`, `gmail`, `resend`, `brevo` |
| `MAIL_FROM` | Sender address (default `email_list@murach.com`) |
| `SMTP_HOST`, `SMTP_PORT` | Override the host/port (local: `localhost:25`, gmail: `smtp.gmail.com:465`) |
| `SMTP_USERNAME`, `SMTP_PASSWORD` | Gmail account + **App Password** (requires 2-Step Verification) |
| `RESEND_API_KEY` | API key from resend.com (for `MAIL_MODE=resend`) |
| `BREVO_API_KEY` | API key from brevo.com (for `MAIL_MODE=brevo`) |
| `MAIL_FROM_NAME` | Sender display name (optional, used by Brevo) |
| `ADMIN_EMAIL` | Gets a BCC copy of every welcome email (optional) |
| `ADMIN_PASSWORD` | Enables the `/admin` page (disabled when empty) |
| `MAIL_DEBUG` | `true` to call `session.setDebug(true)` |

## Local run

```bash
mvn package
```

```bash
docker build -t ch14-email .
```

```bash
docker run --rm -p 8080:8080 -e ADMIN_PASSWORD=change-me ch14-email
```

Open http://localhost:8080. To actually send mail through Gmail, add `-e MAIL_MODE=gmail -e SMTP_USERNAME=you@gmail.com -e SMTP_PASSWORD=<app-password> -e MAIL_FROM=you@gmail.com`.

## Deploy to Render

1. On Render: **New → Blueprint**, pick this repo (Render reads `render.yaml`). Or use **New → Web Service → Docker**.
2. Render builds the `Dockerfile`, injects `PORT`, and the container listens on that port. The health check is `/health`.
3. Set the environment variables in **Environment**:
   - The **Free** plan blocks outbound SMTP ports 25/465/587, so use an HTTPS API:
   - **Brevo (recommended, no domain needed)** – sends to **any** recipient, 300 emails/day free:
     1. Sign up at brevo.com and confirm your account.
     2. **Senders, Domains & Dedicated IPs → Senders → Add a sender** with your email (e.g. Gmail), then click the confirmation link Brevo emails you.
     3. **SMTP & API → API Keys → Generate a new API key** (`xkeysib-...`).
     4. On Render set `MAIL_MODE=brevo`, `BREVO_API_KEY=<key>`, `MAIL_FROM=<the verified sender>`, and optionally `MAIL_FROM_NAME`.
     5. If you get a 401 "unauthorized IP address" error: in Brevo go to **Security → Authorised IPs** and turn off IP blocking (Render has no fixed IP on the free plan).
     6. Mail sent from an @gmail.com address may land in spam, so tell recipients to check their Spam folder.
   - **Resend**: `MAIL_MODE=resend` + `RESEND_API_KEY`. **Without a verified domain Resend is in testing mode**: you must set `MAIL_FROM=onboarding@resend.dev`, and you can **only send to the email address you signed up to Resend with**. Any other recipient returns HTTP 403 `You can only send testing emails to your own email address`. Sending to others requires verifying a domain at resend.com/domains.
   - On a paid plan, you can use `MAIL_MODE=gmail`.
4. Note: `UserDB` lives in memory, so the list resets when the free service sleeps or redeploys.

---

## Chapter 14 – Questions & answers

### Objectives

**Applied 1 – Develop servlets that send email messages to the users of the application.**
`EmailListServlet` receives the form → validates it → saves the `User` → builds the welcome email (`WelcomeEmail`) → sends it through `MailService` (`MailUtilLocal` / `MailUtilGmail` / Resend / Brevo). On a `MessagingException` it logs the full email and shows `errorMessage` on `thanks.jsp`.

**Knowledge 1 – In terms of SMTP, POP and MIME, describe how an email message is sent from one client to another.**

```
Mail client (sender) --SMTP--> Sending mail server --SMTP--> Receiving mail server --POP/IMAP--> Mail client (recipient)
```

1. The sender's mail client composes the message. The content is packaged in **MIME** format, which declares the content type (text/plain, text/html, attachments…) and the charset.
2. The client sends the message to the sender's mail server with **SMTP**.
3. That server relays the message to the recipient's mail server, also with **SMTP**.
4. The message is stored in the mailbox on the receiving server. The recipient's client uses **POP** to download it (or **IMAP** to read it on the server).
5. The receiving client reads the MIME headers to know how to display the content.

### Review questions

1. **What are SMTP / POP / IMAP / MIME?**
   - SMTP sends mail between servers (ports 25, 465 SSL, 587 STARTTLS).
   - POP downloads mail from the server to the client.
   - IMAP lets you read mail stored on the server.
   - MIME specifies the type of content in a message or attachment.
2. **Which JARs does JavaMail need?** `javax.mail.jar` (the JavaMail API) and `activation.jar` (JAF). Copy them into `WEB-INF/lib` and add them to the classpath; with Maven you just declare the dependencies. JAF was removed from the JDK in Java 11, so it must be added explicitly.
3. **The three packages:**
   - `java.util`: `Properties`.
   - `javax.mail`: `Session`, `Message`, `Address`, `Transport`, `MessagingException`.
   - `javax.mail.internet`: `MimeMessage`, `InternetAddress`.
4. **The four steps:**
   1. Get a `Session` from `Properties`.
   2. Create a `MimeMessage` (subject, body).
   3. Address it (`setFrom`, `setRecipient`).
   4. Send it with `Transport`.
5. **Session properties:**
   - `mail.transport.protocol` (smtp/smtps), `mail.smtp.host`, `mail.smtp.port`, `mail.smtp.auth`.
   - `mail.smtp.quitwait=false` avoids an `SSLException` with Gmail.
   - Local server: `smtp`/`localhost`/`25`. Remote server: `smtps`/`smtp.gmail.com`/`465`/`auth=true`. With `smtps` the property prefix becomes `mail.smtps.*`.
   - `setDebug(true)` writes the SMTP conversation to the log.
6. **Why restart Tomcat after changing properties?** `getDefaultInstance` creates the session once and reuses it for the whole JVM, so new properties are ignored. Use `getInstance` to avoid this.
7. **`setText` vs `setContent`:**
   - `setText(body)` → text/plain.
   - `setContent(html, "text/html")` → HTML email.
8. **Addresses:**
   - `setFrom(new InternetAddress(...))`.
   - `setRecipient(RecipientType.TO/CC/BCC, addr)`.
   - Display name: `new InternetAddress(email, name)`.
   - Multiple recipients: `setRecipients(type, Address[])`.
   - Add to the existing list: `addRecipient(s)`.
   - BCC recipients are hidden from each other.
9. **Authentication:**
   - None: `Transport.send(message)`.
   - Required: `session.getTransport()` → `connect(user, pass)` → `sendMessage(msg, msg.getAllRecipients())` → `close()`.
   - A failed send throws `SendFailedException`.
10. **Error handling in the servlet:** wrap the send in `try/catch (MessagingException)`, set the `errorMessage` attribute for the JSP, and `log()` the whole email (TO/FROM/SUBJECT/body) so it can be resent. The user is still added to the list.
11. **Security:**
    - Never hard-code passwords; use env vars and a Gmail App Password.
    - Validate and escape input (XSS, header injection).
    - When the host blocks SMTP, use an HTTPS email API.

### Exercise 14-1 – requirement mapping

| Requirement | Where it's done |
|---|---|
| Send an email when a user joins the list | `EmailListServlet`, `WelcomeEmail` |
| Local SMTP server | `MAIL_MODE=local` → `MailUtilLocal` |
| Remote SMTP (Gmail) with authentication | `MAIL_MODE=gmail` → `MailUtilGmail` |
| HTML email | "HTML" option on the form → `setContent(body, "text/html")` |
| CC/BCC, multiple recipients | `ADMIN_EMAIL` (BCC), `/admin` (`setRecipients`) |
| Show the error + log it | `thanks.jsp` + `log(...)` |

## Project structure

```
src/main/java/murach/business   User, UserValidator
src/main/java/murach/data       UserDB (in-memory)
src/main/java/murach/util       Email, MailConfig, MailUtil, MailUtilLocal, MailUtilGmail, MailUtilResend, MailUtilBrevo,
                                MailService, MailServiceInfo, MailLog, HtmlUtil
src/main/java/murach/email      EmailListServlet, WelcomeEmail, AdminServlet, AdminAuth, MailServiceServlet, HealthServlet
src/main/webapp                 index.jsp, thanks.jsp, questions.jsp, WEB-INF/{web.xml, admin.jsp, mail.jsp, error.jsp}
Dockerfile, docker-entrypoint.sh, render.yaml
```
