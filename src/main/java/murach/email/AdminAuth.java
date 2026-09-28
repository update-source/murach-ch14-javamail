package murach.email;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import murach.util.MailConfig;

/** Session-based admin login and CSRF tokens shared by /admin and /mail. */
final class AdminAuth {

    static final String ADMIN_ATTR = "isAdmin";
    static final String CSRF_ATTR = "csrfToken";
    private static final SecureRandom RANDOM = new SecureRandom();

    private AdminAuth() {
    }

    /** Admin features are disabled unless ADMIN_PASSWORD is set. */
    static boolean enabled() {
        return !MailConfig.adminPassword().isEmpty();
    }

    static boolean isAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && Boolean.TRUE.equals(session.getAttribute(ADMIN_ATTR));
    }

    /** @return true if the password was correct and the user is now logged in. */
    static boolean login(HttpServletRequest request) {
        if (!enabled() || !matches(request.getParameter("password"), MailConfig.adminPassword())) {
            return false;
        }
        request.getSession();
        request.changeSessionId();   // prevent session fixation
        request.getSession().setAttribute(ADMIN_ATTR, Boolean.TRUE);
        return true;
    }

    static void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    /** Creates the session's CSRF token if needed; JSPs read it as sessionScope.csrfToken. */
    static void ensureCsrfToken(HttpServletRequest request) {
        HttpSession session = request.getSession();
        if (session.getAttribute(CSRF_ATTR) == null) {
            byte[] token = new byte[24];
            RANDOM.nextBytes(token);
            session.setAttribute(CSRF_ATTR, Base64.getUrlEncoder().encodeToString(token));
        }
    }

    /** True if the request comes from a logged-in admin and carries the right CSRF token. */
    static boolean isAuthorizedPost(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return isAdmin(request)
                && matches(request.getParameter("csrf"), (String) session.getAttribute(CSRF_ATTR));
    }

    /** Constant-time comparison. */
    static boolean matches(String given, String expected) {
        if (given == null || expected == null) {
            return false;
        }
        return MessageDigest.isEqual(given.getBytes(StandardCharsets.UTF_8),
                expected.getBytes(StandardCharsets.UTF_8));
    }
}
