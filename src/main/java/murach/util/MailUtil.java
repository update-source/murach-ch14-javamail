package murach.util;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/** Shared code for steps 2 and 3: create a message and address it. */
public final class MailUtil {

    private static final String UTF8 = StandardCharsets.UTF_8.name();

    private MailUtil() {
    }

    public static MimeMessage buildMessage(Session session, Email email) throws MessagingException {
        // 2 - create a message
        MimeMessage message = new MimeMessage(session);
        message.setSubject(email.subject(), UTF8);
        if (email.html()) {
            message.setContent(email.body(), "text/html; charset=" + UTF8);
        } else {
            message.setText(email.body(), UTF8);   // MIME type text/plain
        }

        // 3 - address the message
        message.setFrom(new InternetAddress(email.from()));
        message.setRecipients(Message.RecipientType.TO, toAddresses(email.to()));
        if (!email.cc().isEmpty()) {
            message.setRecipients(Message.RecipientType.CC, toAddresses(email.cc()));
        }
        if (!email.bcc().isEmpty()) {
            message.setRecipients(Message.RecipientType.BCC, toAddresses(email.bcc()));
        }
        message.setSentDate(new Date());
        return message;
    }

    static Address[] toAddresses(List<String> emails) throws AddressException {
        Address[] addresses = new Address[emails.size()];
        for (int i = 0; i < emails.size(); i++) {
            addresses[i] = new InternetAddress(emails.get(i), true);
        }
        return addresses;
    }
}
