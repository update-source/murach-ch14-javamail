package murach.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class UserValidatorTest {

    @Test
    void validUserHasNoErrors() {
        assertTrue(UserValidator.validate(new User("Andrea", "Steelman", "andi@yahoo.com")).isEmpty());
    }

    @Test
    void acceptsVietnameseNames() {
        assertTrue(UserValidator.validate(new User("Nguyễn", "Văn A", "a.nguyen@example.vn")).isEmpty());
    }

    @Test
    void blankFieldsAreReported() {
        List<String> errors = UserValidator.validate(new User(" ", "", null));
        assertEquals(3, errors.size());
    }

    @Test
    void rejectsInvalidEmails() {
        assertFalse(UserValidator.isValidEmail("not-an-email"));
        assertFalse(UserValidator.isValidEmail("a@b"));
        assertFalse(UserValidator.isValidEmail("a@b.com\r\nBcc: evil@x.com"));
        assertTrue(UserValidator.isValidEmail("jsmith+list@gmail.com"));
    }

    @Test
    void rejectsTooLongName() {
        String longName = "x".repeat(UserValidator.MAX_NAME_LENGTH + 1);
        assertEquals(1, UserValidator.validate(new User(longName, "B", "a@b.com")).size());
    }
}
