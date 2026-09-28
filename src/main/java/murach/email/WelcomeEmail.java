package murach.email;

import java.util.List;

import murach.business.User;
import murach.util.Email;
import murach.util.HtmlUtil;

/** Builds the "Welcome to our email list" message from the slides. */
public final class WelcomeEmail {

    static final String SUBJECT = "Welcome to our email list";

    private WelcomeEmail() {
    }

    public static Email create(User user, String from, boolean html, String bccCopyTo) {
        String body = html ? htmlBody(user) : textBody(user);
        List<String> bcc = bccCopyTo == null || bccCopyTo.isBlank() ? List.of() : List.of(bccCopyTo);
        return Email.of(user.getEmail(), from, SUBJECT, body, html).withBcc(bcc);
    }

    static String textBody(User user) {
        return "Dear " + user.getFirstName() + ",\n\n"
                + "Thanks for joining our email list. "
                + "We'll make sure to send "
                + "you announcements about new products "
                + "and promotions.\n"
                + "Have a great day and thanks again!\n\n"
                + "Kelly Slivkoff\n"
                + "Mike Murach & Associates";
    }

    static String htmlBody(User user) {
        String name = HtmlUtil.escape(user.getFirstName());
        return "<!DOCTYPE html><html><body style=\"font-family:Arial,sans-serif;color:#222\">"
                + "<h2 style=\"color:#0033cc\">Welcome to our email list!</h2>"
                + "<p>Dear " + name + ",</p>"
                + "<p>Thanks for joining our email list. We'll make sure to send you "
                + "announcements about <b>new products</b> and <b>promotions</b>.</p>"
                + "<p>Have a great day and thanks again!</p>"
                + "<p>Kelly Slivkoff<br>Mike Murach &amp; Associates</p>"
                + "</body></html>";
    }
}
