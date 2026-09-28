package murach.util;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.mail.MessagingException;

import murach.util.MailConfig.Mode;

/** Picks the delivery helper based on MAIL_MODE and records every attempt in MailLog. */
public final class MailService {

    private static final Logger LOG = Logger.getLogger(MailService.class.getName());

    private MailService() {
    }

    /** Sends with the default service (MAIL_MODE). */
    public static boolean send(Email email) throws MessagingException {
        return send(email, MailConfig.mode());
    }

    /**
     * @return true if the email was handed to a mail server/API,
     *         false if mode is LOG (the email was only logged).
     */
    public static boolean send(Email email, Mode mode) throws MessagingException {
        try {
            switch (mode) {
                case LOCAL -> MailUtilLocal.sendMail(email);
                case GMAIL -> MailUtilGmail.sendMail(email);
                case RESEND -> MailUtilResend.sendMail(email);
                case BREVO -> MailUtilBrevo.sendMail(email);
                default -> {
                    LOG.info(() -> "MAIL_MODE=log, email not sent:\n" + describe(email));
                    MailLog.add(mode, email, "LOGGED", "Chỉ ghi log, không gửi");
                    return false;
                }
            }
        } catch (MessagingException e) {
            MailLog.add(mode, email, "FAILED", e.getMessage());
            throw e;
        }
        LOG.info(() -> "Email sent via " + mode + " to " + email.to()
                + " (cc " + email.cc().size() + ", bcc " + email.bcc().size() + ")");
        MailLog.add(mode, email, "SENT", "Đã gửi");
        return true;
    }

    /** Verifies that the service can connect/authenticate, without sending. */
    public static void testConnection(Mode mode) throws MessagingException {
        switch (mode) {
            case LOCAL -> MailUtilLocal.testConnection();
            case GMAIL -> MailUtilGmail.testConnection();
            case RESEND -> MailUtilResend.testConnection();
            case BREVO -> MailUtilBrevo.testConnection();
            default -> { /* LOG mode needs no connection */ }
        }
    }

    public static boolean isConfigured(Mode mode) {
        return switch (mode) {
            case LOG, LOCAL -> true;
            case GMAIL -> MailUtilGmail.isConfigured();
            case RESEND -> !MailConfig.resendApiKey().isEmpty();
            case BREVO -> !MailConfig.brevoApiKey().isEmpty();
        };
    }

    /** One entry per service, for the Mail Services page. Never includes secrets. */
    public static List<MailServiceInfo> services() {
        Mode active = MailConfig.mode();
        List<MailServiceInfo> list = new ArrayList<>();
        list.add(info(Mode.LOG, "Log (demo)",
                "Không gửi thật; ghi email vào log server. Mặc định, an toàn để demo.",
                "stdout / catalina log", active));
        list.add(info(Mode.LOCAL, "Local SMTP (MailUtilLocal)",
                "SMTP server cục bộ, không cần xác thực – Transport.send(message).",
                MailUtilLocal.settings(), active));
        list.add(info(Mode.GMAIL, "Gmail SMTPS (MailUtilGmail)",
                "SMTP server từ xa, có xác thực – getTransport / connect / sendMessage / close.",
                MailUtilGmail.settings(), active));
        list.add(info(Mode.RESEND, "Resend HTTPS API",
                "Gửi qua HTTPS (cổng 443) – dùng được trên Render free (chặn cổng SMTP).",
                "https://api.resend.com · key " + (MailConfig.resendApiKey().isEmpty() ? "(chưa đặt)" : "re_***"),
                active));
        list.add(info(Mode.BREVO, "Brevo HTTPS API",
                "Gửi qua HTTPS tới mọi người nhận (300 email/ngày miễn phí); "
                        + "chỉ cần xác minh email người gửi, không cần domain.",
                "https://api.brevo.com · key " + (MailConfig.brevoApiKey().isEmpty() ? "(chưa đặt)" : "xkeysib-***"),
                active));
        return list;
    }

    private static MailServiceInfo info(Mode mode, String name, String description,
            String settings, Mode active) {
        return new MailServiceInfo(mode, name, description, settings, isConfigured(mode), mode == active);
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
