package murach.util;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

import javax.mail.MessagingException;

/**
 * Sends email through the Brevo (ex-Sendinblue) HTTPS API
 * (https://developers.brevo.com/reference/sendtransacemail).
 * Unlike Resend's testing mode, Brevo can deliver to any recipient without a
 * domain: MAIL_FROM only has to be a sender verified in Brevo
 * (Senders, Domains & Dedicated IPs → Senders → Add a sender).
 */
public final class MailUtilBrevo {

    private static final URI SEND = URI.create("https://api.brevo.com/v3/smtp/email");
    private static final URI ACCOUNT = URI.create("https://api.brevo.com/v3/account");
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private MailUtilBrevo() {
    }

    public static void sendMail(Email email) throws MessagingException {
        HttpRequest request = request(SEND)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(toJson(email)))
                .build();
        execute(request, "sending email");
    }

    /** Checks the API key with GET /v3/account, without sending an email. */
    public static void testConnection() throws MessagingException {
        execute(request(ACCOUNT).GET().build(), "testing Brevo API");
    }

    private static HttpRequest.Builder request(URI uri) throws MessagingException {
        String apiKey = MailConfig.brevoApiKey();
        if (apiKey.isEmpty()) {
            throw new MessagingException("BREVO_API_KEY must be set for MAIL_MODE=brevo");
        }
        return HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(15))
                .header("api-key", apiKey)
                .header("Accept", "application/json");
    }

    private static void execute(HttpRequest request, String what) throws MessagingException {
        try {
            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) {
                throw new MessagingException(explain(response.statusCode(), response.body()));
            }
        } catch (IOException e) {
            throw new MessagingException("Unable to reach Brevo API: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MessagingException("Interrupted while " + what, e);
        }
    }

    static String explain(int status, String body) {
        String message = "Brevo API returned HTTP " + status + ": " + body;
        if (status == 401) {
            message += " → BREVO_API_KEY sai hoặc đã bị thu hồi"
                    + " (hoặc tài khoản chưa được kích hoạt / IP chưa được cho phép).";
        } else if (body.contains("sender") || body.contains("Sender")) {
            message += " → MAIL_FROM phải là email người gửi đã được xác minh trong Brevo (Senders).";
        }
        return message;
    }

    static String toJson(Email email) {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"sender\":{\"email\":").append(MailUtilResend.quote(email.from()));
        String name = MailConfig.senderName();
        if (!name.isEmpty()) {
            sb.append(",\"name\":").append(MailUtilResend.quote(name));
        }
        sb.append('}');
        sb.append(",\"to\":").append(recipients(email.to()));
        if (!email.cc().isEmpty()) {
            sb.append(",\"cc\":").append(recipients(email.cc()));
        }
        if (!email.bcc().isEmpty()) {
            sb.append(",\"bcc\":").append(recipients(email.bcc()));
        }
        sb.append(",\"subject\":").append(MailUtilResend.quote(email.subject()));
        sb.append(email.html() ? ",\"htmlContent\":" : ",\"textContent\":")
                .append(MailUtilResend.quote(email.body()));
        return sb.append('}').toString();
    }

    /** [{"email":"a@x.com"},{"email":"b@y.com"}] */
    private static String recipients(List<String> emails) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < emails.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append("{\"email\":").append(MailUtilResend.quote(emails.get(i))).append('}');
        }
        return sb.append(']').toString();
    }
}
