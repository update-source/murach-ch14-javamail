package murach.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import javax.mail.MessagingException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import murach.util.MailConfig.Mode;

class MailServiceTest {

    @BeforeEach
    void reset() {
        MailLog.clear();
    }

    @AfterEach
    void clearProps() {
        System.clearProperty("SMTP_USERNAME");
        System.clearProperty("SMTP_PASSWORD");
    }

    @Test
    void logModeRecordsHistoryWithoutSending() throws Exception {
        boolean sent = MailService.send(Email.of("a@x.com", "b@x.com", "Hi", "Body", false), Mode.LOG);
        assertFalse(sent);
        List<MailLog.Entry> history = MailLog.recent();
        assertEquals(1, history.size());
        assertEquals("LOGGED", history.get(0).getStatus());
        assertEquals("log", history.get(0).getMode());
        assertEquals("a@x.com", history.get(0).getTo());
    }

    @Test
    void failedSendIsRecordedAndRethrown() {
        // No SMTP credentials in the test environment -> gmail must fail fast.
        Email email = Email.of("a@x.com", "b@x.com", "Hi", "Body", false);
        assertThrows(MessagingException.class, () -> MailService.send(email, Mode.GMAIL));
        assertEquals("FAILED", MailLog.recent().get(0).getStatus());
    }

    @Test
    void historyIsCappedAndNewestFirst() throws Exception {
        for (int i = 0; i < MailLog.MAX_ENTRIES + 5; i++) {
            MailService.send(Email.of("a@x.com", "b@x.com", "S" + i, "B", false), Mode.LOG);
        }
        List<MailLog.Entry> history = MailLog.recent();
        assertEquals(MailLog.MAX_ENTRIES, history.size());
        assertEquals("S" + (MailLog.MAX_ENTRIES + 4), history.get(0).getSubject());
    }

    @Test
    void configuredStatusFollowsSettings() {
        assertTrue(MailService.isConfigured(Mode.LOG));
        assertTrue(MailService.isConfigured(Mode.LOCAL));
        assertFalse(MailService.isConfigured(Mode.GMAIL));
        System.setProperty("SMTP_USERNAME", "johnsmith@gmail.com");
        System.setProperty("SMTP_PASSWORD", "app-password");
        assertTrue(MailService.isConfigured(Mode.GMAIL));
    }

    @Test
    void servicesListNeverExposesSecrets() {
        System.setProperty("SMTP_USERNAME", "johnsmith@gmail.com");
        System.setProperty("SMTP_PASSWORD", "super-secret");
        List<MailServiceInfo> services = MailService.services();
        assertEquals(4, services.size());
        for (MailServiceInfo s : services) {
            assertFalse(s.getSettings().contains("super-secret"));
            assertFalse(s.getSettings().contains("johnsmith@"));
        }
        assertTrue(services.get(2).getSettings().contains("jo***@gmail.com"));
    }
}
