package murach.util;

import java.util.Locale;

/**
 * Mail settings read from environment variables (or JVM system properties),
 * so no username/password is hard-coded in the source like the book's
 * transport.connect("johnsmith@gmail.com", "sesame") example.
 */
public final class MailConfig {

    /** How email is delivered. */
    public enum Mode {
        /** Don't send; just log the email (safe default for demos). */
        LOG,
        /** MailUtilLocal: SMTP server on localhost:25, no authentication. */
        LOCAL,
        /** MailUtilGmail: remote SMTPS server (Gmail by default) with authentication. */
        GMAIL,
        /** HTTPS API of resend.com - works on hosts that block SMTP ports (e.g. Render free). */
        RESEND
    }

    private MailConfig() {
    }

    public static Mode mode() {
        String value = get("MAIL_MODE", "log").trim().toUpperCase(Locale.ROOT);
        try {
            return Mode.valueOf(value);
        } catch (IllegalArgumentException e) {
            return Mode.LOG;
        }
    }

    public static String from() {
        return get("MAIL_FROM", "email_list@murach.com");
    }

    public static String smtpHost(String defaultHost) {
        return get("SMTP_HOST", defaultHost);
    }

    public static int smtpPort(int defaultPort) {
        try {
            return Integer.parseInt(get("SMTP_PORT", String.valueOf(defaultPort)).trim());
        } catch (NumberFormatException e) {
            return defaultPort;
        }
    }

    public static String smtpUsername() {
        return get("SMTP_USERNAME", "");
    }

    public static String smtpPassword() {
        return get("SMTP_PASSWORD", "");
    }

    public static String resendApiKey() {
        return get("RESEND_API_KEY", "");
    }

    /** Optional address that receives a BCC copy of every welcome email. */
    public static String adminEmail() {
        return get("ADMIN_EMAIL", "");
    }

    /** Password for the /admin page; the page is disabled when empty. */
    public static String adminPassword() {
        return get("ADMIN_PASSWORD", "");
    }

    public static boolean debug() {
        return Boolean.parseBoolean(get("MAIL_DEBUG", "false"));
    }

    static String get(String name, String defaultValue) {
        String value = System.getProperty(name);
        if (value == null || value.isEmpty()) {
            value = System.getenv(name);
        }
        return value == null || value.isEmpty() ? defaultValue : value;
    }
}
