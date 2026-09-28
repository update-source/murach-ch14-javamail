package murach.util;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;

/** Helper class for a local SMTP server that doesn't require authentication. */
public final class MailUtilLocal {

    private MailUtilLocal() {
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

        // 4 - send the message (no authentication required)
        Transport.send(message);
    }

    /** Opens and closes a connection to the SMTP server without sending anything. */
    public static void testConnection() throws MessagingException {
        Transport transport = createSession().getTransport();
        try {
            transport.connect();
        } finally {
            transport.close();
        }
    }

    public static String settings() {
        return "smtp://" + MailConfig.smtpHost("localhost") + ":" + MailConfig.smtpPort(25);
    }

    static Session createSession() {
        Properties props = new Properties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.host", MailConfig.smtpHost("localhost"));
        props.put("mail.smtp.port", MailConfig.smtpPort(25));
        props.put("mail.smtp.connectiontimeout", 10000);
        props.put("mail.smtp.timeout", 10000);
        // getInstance (not getDefaultInstance) so changed properties take effect
        // without restarting Tomcat.
        Session session = Session.getInstance(props);
        session.setDebug(MailConfig.debug());
        return session;
    }
}
