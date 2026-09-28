package murach.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import murach.business.User;

/**
 * Thread-safe in-memory store for the email list. The book's version writes
 * to a database; an in-memory map keeps this demo self-contained so it can run
 * in a single container on Render without a database service.
 */
public final class UserDB {

    private static final Map<String, User> USERS = new LinkedHashMap<>();

    private UserDB() {
    }

    /** @return false if a user with the same email is already on the list. */
    public static synchronized boolean insert(User user) {
        String key = key(user.getEmail());
        if (USERS.containsKey(key)) {
            return false;
        }
        USERS.put(key, user);
        return true;
    }

    public static synchronized boolean emailExists(String email) {
        return USERS.containsKey(key(email));
    }

    public static synchronized User selectUser(String email) {
        return USERS.get(key(email));
    }

    public static synchronized List<User> selectAll() {
        return new ArrayList<>(USERS.values());
    }

    public static synchronized int count() {
        return USERS.size();
    }

    /** For tests. */
    static synchronized void clear() {
        USERS.clear();
    }

    private static String key(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
