package murach.email;

import murach.business.User;

/** Exposes package-private WelcomeEmail helpers to tests in other packages. */
public final class WelcomeEmailAccess {

    private WelcomeEmailAccess() {
    }

    public static String htmlBody(User user) {
        return WelcomeEmail.htmlBody(user);
    }
}
