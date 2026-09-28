package murach.util;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;

/**
 * Helper class for a remote SMTP server that requires authentication
 * (Gmail by default). For Gmail, SMTP_PASSWORD must be an App Password
 * (Google account → Security → 2-Step Verification → App passwords).
 */
public final class MailUtilGmail {

    private MailUtilGmail() {
    }

    public static void sendMail(String to, String from, String subject,
            String body, boolean bodyIsHTML) throws MessagingException {
        sendMail(Email.of(to, from, subject, body, bodyIsHTML));
    }

    public static void sendMail(Email email) throws MessagingException {
        // 1 - get a mail session
        Session session = createSession();

        // 2 & 3 - create and address the message
        Message message = MailUtil.buildMessage(session, email);

        // 4 - send the message (authentication required)
        Transport transport = session.getTransport();
        try {
            transport.connect(MailConfig.smtpUsername(), MailConfig.smtpPassword());
            transport.sendMessage(message, message.getAllRecipients());
        } finally {
            transport.close();
        }
    }

    /** Logs in to the SMTP server without sending anything. */
    public static void testConnection() throws MessagingException {
        Transport transport = createSession().getTransport();
        try {
            transport.connect(MailConfig.smtpUsername(), MailConfig.smtpPassword());
        } finally {
            transport.close();
        }
    }

    public static boolean isConfigured() {
        return !MailConfig.smtpUsername().isEmpty() && !MailConfig.smtpPassword().isEmpty();
    }

    public static String settings() {
        return "smtps://" + MailConfig.smtpHost("smtp.gmail.com") + ":" + MailConfig.smtpPort(465)
                + " · user " + mask(MailConfig.smtpUsername());
    }

    /** "johnsmith@gmail.com" -> "jo***@gmail.com" so the page never shows full credentials. */
    static String mask(String username) {
        if (username.isEmpty()) {
            return "(chưa đặt)";
        }
        int at = username.indexOf('@');
        String local = at < 0 ? username : username.substring(0, at);
        String domain = at < 0 ? "" : username.substring(at);
        return local.substring(0, Math.min(2, local.length())) + "***" + domain;
    }

    static Session createSession() throws MessagingException {
        if (!isConfigured()) {
            throw new MessagingException(
                    "SMTP_USERNAME and SMTP_PASSWORD must be set for MAIL_MODE=gmail");
        }
        Properties props = new Properties();
        props.put("mail.transport.protocol", "smtps");
        props.put("mail.smtps.host", MailConfig.smtpHost("smtp.gmail.com"));
        props.put("mail.smtps.port", MailConfig.smtpPort(465));
        props.put("mail.smtps.auth", "true");
        props.put("mail.smtps.quitwait", "false");
        props.put("mail.smtps.connectiontimeout", 10000);
        props.put("mail.smtps.timeout", 10000);
        Session session = Session.getInstance(props);
        session.setDebug(MailConfig.debug());
        return session;
    }
}
