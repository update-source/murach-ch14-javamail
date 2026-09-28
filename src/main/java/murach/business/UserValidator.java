package murach.business;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/** Validates user input at the system boundary (the HTML form). */
public final class UserValidator {

    static final int MAX_NAME_LENGTH = 50;
    static final int MAX_EMAIL_LENGTH = 254;

    private static final Pattern EMAIL = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$");

    private UserValidator() {
    }

    public static List<String> validate(User user) {
        List<String> errors = new ArrayList<>();
        checkName(user.getFirstName(), "First name", errors);
        checkName(user.getLastName(), "Last name", errors);

        String email = user.getEmail();
        if (isBlank(email)) {
            errors.add("Email is required.");
        } else if (email.length() > MAX_EMAIL_LENGTH || !isValidEmail(email)) {
            errors.add("Email is not a valid email address.");
        }
        return errors;
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL.matcher(email).matches();
    }

    private static void checkName(String value, String label, List<String> errors) {
        if (isBlank(value)) {
            errors.add(label + " is required.");
        } else if (value.length() > MAX_NAME_LENGTH) {
            errors.add(label + " must be at most " + MAX_NAME_LENGTH + " characters.");
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
