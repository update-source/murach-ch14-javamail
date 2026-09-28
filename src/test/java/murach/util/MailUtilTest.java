package murach.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.MimeMessage;

import org.junit.jupiter.api.Test;

import murach.business.User;
import murach.email.WelcomeEmailAccess;

class MailUtilTest {

    private final Session session = Session.getInstance(new Properties());

    @Test
    void plainTextMessageUsesTextPlain() throws Exception {
        MimeMessage m = MailUtil.buildMessage(session,
                Email.of("andi@yahoo.com", "cds@murach.com", "Order Confirmation", "Thanks for your order!", false));
        m.saveChanges();
        assertEquals("Order Confirmation", m.getSubject());
        assertTrue(m.getContentType().startsWith("text/plain"));
        assertEquals("cds@murach.com", m.getFrom()[0].toString());
        assertEquals("andi@yahoo.com", m.getRecipients(Message.RecipientType.TO)[0].toString());
    }

    @Test
    void htmlMessageUsesTextHtml() throws Exception {
        MimeMessage m = MailUtil.buildMessage(session,
                Email.of("andi@yahoo.com", "cds@murach.com", "Hi", "<h1>Thanks for your order!</h1>", true));
        m.saveChanges();
        assertTrue(m.getContentType().startsWith("text/html"));
    }

    @Test
    void setsToCcAndBccRecipients() throws Exception {
        Email email = new Email(List.of("a@x.com", "b@x.com"), List.of("ted@yahoo.com"),
                List.of("jsmith@gmail.com"), "cds@murach.com", "S", "B", false);
        MimeMessage m = MailUtil.buildMessage(session, email);
        assertEquals(2, m.getRecipients(Message.RecipientType.TO).length);
        assertEquals(1, m.getRecipients(Message.RecipientType.CC).length);
        assertEquals(1, m.getRecipients(Message.RecipientType.BCC).length);
        assertEquals(4, m.getAllRecipients().length);
    }

    @Test
    void utf8SubjectRoundTrips() throws Exception {
        MimeMessage m = MailUtil.buildMessage(session,
                Email.of("a@x.com", "b@x.com", "Chào mừng bạn", "Xin chào", false));
        assertEquals("Chào mừng bạn", m.getSubject());
    }

    @Test
    void invalidAddressIsRejected() {
        assertThrows(AddressException.class, () -> MailUtil.buildMessage(session,
                Email.of("bad address", "b@x.com", "S", "B", false)));
    }

    @Test
    void emailNeedsARecipient() {
        assertThrows(IllegalArgumentException.class,
                () -> new Email(List.of(), null, null, "a@x.com", "S", "B", false));
    }

    @Test
    void resendJsonIsEscaped() {
        Email email = new Email(List.of("a@x.com"), null, List.of("b@x.com"),
                "c@x.com", "Say \"hi\"", "line1\nline2", false);
        assertEquals("{\"from\":\"c@x.com\",\"to\":[\"a@x.com\"],\"bcc\":[\"b@x.com\"],"
                + "\"subject\":\"Say \\\"hi\\\"\",\"text\":\"line1\\nline2\"}", MailUtilResend.toJson(email));
    }

    @Test
    void htmlWelcomeEmailEscapesName() {
        User user = new User("<script>", "X", "a@x.com");
        String body = WelcomeEmailAccess.htmlBody(user);
        assertTrue(body.contains("&lt;script&gt;"));
        assertTrue(!body.contains("<script>"));
    }
}
