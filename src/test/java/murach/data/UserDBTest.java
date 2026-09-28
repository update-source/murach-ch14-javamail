package murach.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import murach.business.User;

class UserDBTest {

    @BeforeEach
    void reset() {
        UserDB.clear();
    }

    @Test
    void insertRejectsDuplicateEmailIgnoringCase() {
        assertTrue(UserDB.insert(new User("A", "B", "andi@yahoo.com")));
        assertFalse(UserDB.insert(new User("C", "D", "ANDI@yahoo.com ")));
        assertEquals(1, UserDB.count());
        assertTrue(UserDB.emailExists("Andi@Yahoo.com"));
    }

    @Test
    void selectAllKeepsInsertionOrder() {
        UserDB.insert(new User("A", "A", "a@x.com"));
        UserDB.insert(new User("B", "B", "b@x.com"));
        assertEquals("a@x.com", UserDB.selectAll().get(0).getEmail());
        assertEquals("B", UserDB.selectUser("b@x.com").getFirstName());
    }
}
