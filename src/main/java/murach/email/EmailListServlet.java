package murach.email;

import java.io.IOException;
import java.util.List;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import murach.business.User;
import murach.business.UserValidator;
import murach.data.UserDB;
import murach.util.Email;
import murach.util.MailConfig;
import murach.util.MailService;

/** A servlet that adds a user to the email list and sends a welcome email. */
public class EmailListServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        forward("/index.jsp", request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // get current action
        String action = request.getParameter("action");
        if (action == null) {
            action = "join";  // default action
        }

        // perform action and set URL to appropriate page
        String url = "/index.jsp";
        if (action.equals("add")) {
            url = add(request);
        }
        forward(url, request, response);
    }

    private String add(HttpServletRequest request) {
        // get parameters from the request
        String firstName = trim(request.getParameter("firstName"));
        String lastName = trim(request.getParameter("lastName"));
        String email = trim(request.getParameter("email"));
        boolean isBodyHTML = "html".equals(request.getParameter("format"));

        // store data in User object
        User user = new User(firstName, lastName, email);
        request.setAttribute("user", user);
        request.setAttribute("format", isBodyHTML ? "html" : "text");

        List<String> errors = UserValidator.validate(user);
        if (!errors.isEmpty()) {
            request.setAttribute("errors", errors);
            return "/index.jsp";
        }
        if (!UserDB.insert(user)) {
            request.setAttribute("errors",
                    List.of("The email address " + email + " is already on our list."));
            return "/index.jsp";
        }

        // send email to user (and a BCC copy to the admin, if configured)
        String from = MailConfig.from();
        Email welcome = WelcomeEmail.create(user, from, isBodyHTML, MailConfig.adminEmail());
        request.setAttribute("mailMode", MailConfig.mode().name().toLowerCase());
        try {
            boolean sent = MailService.send(welcome);
            request.setAttribute("emailSent", sent);
            if (!sent) {
                request.setAttribute("emailPreview", welcome.body());
                request.setAttribute("emailPreviewHtml", isBodyHTML);
            }
        } catch (MessagingException e) {
            String errorMessage = "ERROR: Unable to send email. "
                    + "Check the server logs for details. "
                    + "NOTE: You may need to configure the MAIL_* environment variables "
                    + "as described in the README. "
                    + "ERROR MESSAGE: " + e.getMessage();
            request.setAttribute("errorMessage", errorMessage);
            log("Unable to send email. \n"
                    + "Here is the email you tried to send: \n"
                    + MailService.describe(welcome), e);
        }
        return "/thanks.jsp";
    }

    private void forward(String url, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        getServletContext().getRequestDispatcher(url).forward(request, response);
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
