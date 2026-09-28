package murach.email;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import murach.business.UserValidator;
import murach.util.Email;
import murach.util.MailConfig;
import murach.util.MailConfig.Mode;
import murach.util.MailLog;
import murach.util.MailService;

/**
 * Mail Services page (/mail): shows every delivery service and its status,
 * lets an admin test a connection, compose an email (To/CC/BCC, text/html)
 * through a chosen service, and see the send history.
 */
public class MailServiceServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    static final int MAX_RECIPIENTS = 50;
    static final int MAX_SUBJECT = 200;
    static final int MAX_BODY = 20_000;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        show(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = String.valueOf(request.getParameter("action"));
        if (action.equals("login")) {
            if (!AdminAuth.login(request)) {
                request.setAttribute("errors", List.of("Sai mật khẩu admin."));
            }
        } else if (!AdminAuth.isAuthorizedPost(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        } else if (action.equals("send")) {
            send(request);
        } else if (action.equals("test")) {
            test(request);
        }
        show(request, response);
    }

    private void send(HttpServletRequest request) {
        List<String> errors = new ArrayList<>();
        Mode mode = parseMode(request.getParameter("mode"), errors);
        List<String> to = parseAddresses(request.getParameter("to"), "To", errors);
        List<String> cc = parseAddresses(request.getParameter("cc"), "CC", errors);
        List<String> bcc = parseAddresses(request.getParameter("bcc"), "BCC", errors);
        String subject = trim(request.getParameter("subject"));
        String body = trim(request.getParameter("body"));
        boolean html = "html".equals(request.getParameter("format"));

        if (to.isEmpty()) {
            errors.add("Cần ít nhất một người nhận ở ô To.");
        }
        if (to.size() + cc.size() + bcc.size() > MAX_RECIPIENTS) {
            errors.add("Tối đa " + MAX_RECIPIENTS + " người nhận cho mỗi email.");
        }
        if (subject.isEmpty() || subject.length() > MAX_SUBJECT) {
            errors.add("Subject bắt buộc (tối đa " + MAX_SUBJECT + " ký tự).");
        }
        if (body.isEmpty() || body.length() > MAX_BODY) {
            errors.add("Nội dung bắt buộc (tối đa " + MAX_BODY + " ký tự).");
        }
        if (mode != null && !MailService.isConfigured(mode)) {
            errors.add("Dịch vụ " + mode.name().toLowerCase() + " chưa được cấu hình.");
        }
        if (!errors.isEmpty()) {
            request.setAttribute("errors", errors);
            return;
        }

        Email email = new Email(to, cc, bcc, MailConfig.from(), subject, body, html);
        try {
            boolean sent = MailService.send(email, mode);
            request.setAttribute("message", sent
                    ? "Đã gửi email tới " + (to.size() + cc.size() + bcc.size())
                        + " người nhận qua " + mode.name().toLowerCase() + "."
                    : "Chế độ log: email đã được ghi vào log server, không gửi thật.");
            request.setAttribute("sentOk", true);
        } catch (MessagingException e) {
            log("Unable to send email:\n" + MailService.describe(email), e);
            request.setAttribute("errors", List.of("ERROR: Không gửi được email. " + e.getMessage()));
        }
    }

    private void test(HttpServletRequest request) {
        List<String> errors = new ArrayList<>();
        Mode mode = parseMode(request.getParameter("mode"), errors);
        if (mode == null) {
            request.setAttribute("errors", errors);
            return;
        }
        try {
            MailService.testConnection(mode);
            request.setAttribute("message", "Kết nối tới dịch vụ " + mode.name().toLowerCase() + " thành công.");
        } catch (MessagingException e) {
            log("Connection test failed for " + mode, e);
            request.setAttribute("errors", List.of("Kiểm tra " + mode.name().toLowerCase()
                    + " thất bại: " + e.getMessage()));
        }
    }

    private void show(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("adminEnabled", AdminAuth.enabled());
        request.setAttribute("services", MailService.services());
        request.setAttribute("activeMode", MailConfig.mode().name().toLowerCase());
        request.setAttribute("mailFrom", MailConfig.from());
        if (AdminAuth.isAdmin(request)) {
            AdminAuth.ensureCsrfToken(request);
            request.setAttribute("loggedIn", true);
            request.setAttribute("history", MailLog.recent());
        }
        getServletContext().getRequestDispatcher("/WEB-INF/mail.jsp").forward(request, response);
    }

    static Mode parseMode(String value, List<String> errors) {
        try {
            return Mode.valueOf(String.valueOf(value).trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            errors.add("Dịch vụ gửi mail không hợp lệ.");
            return null;
        }
    }

    /** Splits "a@x.com, b@y.com; c@z.com" and validates each address. */
    static List<String> parseAddresses(String value, String label, List<String> errors) {
        List<String> result = new ArrayList<>();
        if (value == null || value.isBlank()) {
            return result;
        }
        for (String part : value.split("[,;\\s]+")) {
            if (part.isEmpty()) {
                continue;
            }
            if (UserValidator.isValidEmail(part)) {
                result.add(part);
            } else {
                errors.add(label + ": địa chỉ không hợp lệ \"" + part + "\".");
            }
        }
        return result;
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
