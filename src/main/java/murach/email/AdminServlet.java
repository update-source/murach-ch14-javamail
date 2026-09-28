package murach.email;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import murach.business.User;
import murach.data.UserDB;
import murach.util.Email;
import murach.util.MailConfig;
import murach.util.MailService;

/**
 * Admin page: lists subscribers and sends one announcement to the whole list.
 * Demonstrates sending a message to multiple recipients with setRecipients:
 * subscribers go in BCC so they can't see each other's addresses.
 */
public class AdminServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String ADMIN_ATTR = "isAdmin";
    private static final String CSRF_ATTR = "csrfToken";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        show(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String password = MailConfig.adminPassword();
        if (password.isEmpty()) {
            show(request, response);
            return;
        }
        String action = String.valueOf(request.getParameter("action"));
        HttpSession session = request.getSession();

        if (action.equals("login")) {
            if (matches(request.getParameter("password"), password)) {
                request.changeSessionId();   // prevent session fixation
                session.setAttribute(ADMIN_ATTR, Boolean.TRUE);
            } else {
                request.setAttribute("errors", List.of("Wrong password."));
            }
        } else if (!isAdmin(session) || !matches(request.getParameter("csrf"),
                (String) session.getAttribute(CSRF_ATTR))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        } else if (action.equals("logout")) {
            session.invalidate();
            response.sendRedirect(request.getContextPath() + "/admin");
            return;
        } else if (action.equals("broadcast")) {
            broadcast(request);
        }
        show(request, response);
    }

    private void broadcast(HttpServletRequest request) {
        String subject = trim(request.getParameter("subject"));
        String body = trim(request.getParameter("body"));
        boolean html = "html".equals(request.getParameter("format"));
        List<String> errors = new ArrayList<>();
        if (subject.isEmpty() || subject.length() > 200) {
            errors.add("Subject is required (max 200 characters).");
        }
        if (body.isEmpty()) {
            errors.add("Body is required.");
        }
        List<String> bcc = new ArrayList<>();
        for (User u : UserDB.selectAll()) {
            bcc.add(u.getEmail());
        }
        if (bcc.isEmpty()) {
            errors.add("The email list is empty.");
        }
        if (!errors.isEmpty()) {
            request.setAttribute("errors", errors);
            return;
        }
        // To: the list's own address; every subscriber is a BCC recipient.
        String from = MailConfig.from();
        Email email = new Email(List.of(from), null, bcc, from, subject, body, html);
        try {
            boolean sent = MailService.send(email);
            request.setAttribute("message", sent
                    ? "Announcement sent to " + bcc.size() + " subscriber(s)."
                    : "MAIL_MODE=log: announcement for " + bcc.size()
                        + " subscriber(s) was written to the server log, not sent.");
        } catch (MessagingException e) {
            log("Unable to send announcement:\n" + MailService.describe(email), e);
            request.setAttribute("errors", List.of("ERROR: Unable to send email. " + e.getMessage()));
        }
    }

    private void show(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("adminEnabled", !MailConfig.adminPassword().isEmpty());
        HttpSession session = request.getSession(false);
        if (session != null && isAdmin(session)) {
            if (session.getAttribute(CSRF_ATTR) == null) {
                byte[] token = new byte[24];
                RANDOM.nextBytes(token);
                session.setAttribute(CSRF_ATTR, Base64.getUrlEncoder().encodeToString(token));
            }
            request.setAttribute("loggedIn", true);
            request.setAttribute("users", UserDB.selectAll());
            request.setAttribute("mailMode", MailConfig.mode().name().toLowerCase());
        }
        getServletContext().getRequestDispatcher("/WEB-INF/admin.jsp").forward(request, response);
    }

    private static boolean isAdmin(HttpSession session) {
        return Boolean.TRUE.equals(session.getAttribute(ADMIN_ATTR));
    }

    /** Constant-time comparison. */
    private static boolean matches(String given, String expected) {
        if (given == null || expected == null) {
            return false;
        }
        return MessageDigest.isEqual(given.getBytes(StandardCharsets.UTF_8),
                expected.getBytes(StandardCharsets.UTF_8));
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
