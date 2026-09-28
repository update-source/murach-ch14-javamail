package murach.util;

import java.util.logging.Logger;

import javax.mail.MessagingException;

/** Picks the delivery helper based on MAIL_MODE. */
public final class MailService {

    private static final Logger LOG = Logger.getLogger(MailService.class.getName());

    private MailService() {
    }

    /**
     * @return true if the email was handed to a mail server/API,
     *         false if MAIL_MODE=log (the email was only logged).
     */
    public static boolean send(Email email) throws MessagingException {
        MailConfig.Mode mode = MailConfig.mode();
        switch (mode) {
            case LOCAL -> MailUtilLocal.sendMail(email);
            case GMAIL -> MailUtilGmail.sendMail(email);
            case RESEND -> MailUtilResend.sendMail(email);
            default -> {
                LOG.info(() -> "MAIL_MODE=log, email not sent:\n" + describe(email));
                return false;
            }
        }
        LOG.info(() -> "Email sent via " + mode + " to " + email.to()
                + " (cc " + email.cc().size() + ", bcc " + email.bcc().size() + ")");
        return true;
    }

    public static String describe(Email email) {
        return "==================================\n"
                + "TO: " + String.join(", ", email.to()) + "\n"
                + (email.cc().isEmpty() ? "" : "CC: " + String.join(", ", email.cc()) + "\n")
                + (email.bcc().isEmpty() ? "" : "BCC: " + email.bcc().size() + " recipient(s)\n")
                + "FROM: " + email.from() + "\n"
                + "SUBJECT: " + email.subject() + "\n"
                + "CONTENT-TYPE: " + (email.html() ? "text/html" : "text/plain") + "\n\n"
                + email.body() + "\n";
    }
}
