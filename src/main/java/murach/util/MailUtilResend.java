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
 * Sends email through the Resend HTTPS API (https://resend.com/docs/api-reference/emails/send-email).
 * Render's free instances block outbound SMTP ports 25/465/587, so an HTTP
 * API is the practical way to deliver mail from there.
 */
public final class MailUtilResend {

    private static final URI ENDPOINT = URI.create("https://api.resend.com/emails");
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private MailUtilResend() {
    }

    public static void sendMail(Email email) throws MessagingException {
        String apiKey = MailConfig.resendApiKey();
        if (apiKey.isEmpty()) {
            throw new MessagingException("RESEND_API_KEY must be set for MAIL_MODE=resend");
        }
        HttpRequest request = HttpRequest.newBuilder(ENDPOINT)
                .timeout(Duration.ofSeconds(15))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(toJson(email)))
                .build();
        try {
            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) {
                throw new MessagingException("Resend API returned HTTP "
                        + response.statusCode() + ": " + response.body());
            }
        } catch (IOException e) {
            throw new MessagingException("Unable to reach Resend API: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MessagingException("Interrupted while sending email", e);
        }
    }

    static String toJson(Email email) {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"from\":").append(quote(email.from()));
        sb.append(",\"to\":").append(array(email.to()));
        if (!email.cc().isEmpty()) {
            sb.append(",\"cc\":").append(array(email.cc()));
        }
        if (!email.bcc().isEmpty()) {
            sb.append(",\"bcc\":").append(array(email.bcc()));
        }
        sb.append(",\"subject\":").append(quote(email.subject()));
        sb.append(email.html() ? ",\"html\":" : ",\"text\":").append(quote(email.body()));
        return sb.append('}').toString();
    }

    private static String array(List<String> values) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(quote(values.get(i)));
        }
        return sb.append(']').toString();
    }

    static String quote(String s) {
        StringBuilder sb = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.append('"').toString();
    }
}
