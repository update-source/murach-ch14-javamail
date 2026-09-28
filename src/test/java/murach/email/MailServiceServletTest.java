package murach.email;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import murach.util.MailConfig.Mode;

class MailServiceServletTest {

    @Test
    void parsesCommaSemicolonAndSpaceSeparatedAddresses() {
        List<String> errors = new ArrayList<>();
        List<String> result = MailServiceServlet.parseAddresses(
                "a@x.com, b@y.com;c@z.com  d@w.org", "To", errors);
        assertEquals(List.of("a@x.com", "b@y.com", "c@z.com", "d@w.org"), result);
        assertTrue(errors.isEmpty());
    }

    @Test
    void reportsInvalidAddresses() {
        List<String> errors = new ArrayList<>();
        List<String> result = MailServiceServlet.parseAddresses("a@x.com, nope", "CC", errors);
        assertEquals(List.of("a@x.com"), result);
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).startsWith("CC"));
    }

    @Test
    void blankInputGivesEmptyList() {
        List<String> errors = new ArrayList<>();
        assertTrue(MailServiceServlet.parseAddresses("  ", "BCC", errors).isEmpty());
        assertTrue(MailServiceServlet.parseAddresses(null, "BCC", errors).isEmpty());
        assertTrue(errors.isEmpty());
    }

    @Test
    void parsesModeCaseInsensitively() {
        List<String> errors = new ArrayList<>();
        assertEquals(Mode.RESEND, MailServiceServlet.parseMode(" Resend ", errors));
        assertNull(MailServiceServlet.parseMode("sendgrid", errors));
        assertNull(MailServiceServlet.parseMode(null, errors));
        assertEquals(2, errors.size());
    }
}
