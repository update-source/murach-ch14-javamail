package murach.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/** In-memory history of the most recent send attempts (newest first). */
public final class MailLog {

    static final int MAX_ENTRIES = 50;
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ZoneId ZONE = ZoneId.of(MailConfig.get("TZ", "Asia/Ho_Chi_Minh"));
    private static final Deque<Entry> ENTRIES = new ArrayDeque<>();

    private MailLog() {
    }

    /** One send attempt. Getters are used by JSP EL. */
    public static final class Entry {
        private final String time;
        private final String mode;
        private final String to;
        private final int recipientCount;
        private final String subject;
        private final String status;
        private final String detail;

        Entry(String mode, Email email, String status, String detail) {
            this.time = LocalDateTime.now(ZONE).format(TIME);
            this.mode = mode;
            this.to = String.join(", ", email.to());
            this.recipientCount = email.to().size() + email.cc().size() + email.bcc().size();
            this.subject = email.subject();
            this.status = status;
            this.detail = detail;
        }

        public String getTime() { return time; }
        public String getMode() { return mode; }
        public String getTo() { return to; }
        public int getRecipientCount() { return recipientCount; }
        public String getSubject() { return subject; }
        public String getStatus() { return status; }
        public String getDetail() { return detail; }
    }

    public static synchronized void add(MailConfig.Mode mode, Email email, String status, String detail) {
        ENTRIES.addFirst(new Entry(mode.name().toLowerCase(), email, status, detail));
        while (ENTRIES.size() > MAX_ENTRIES) {
            ENTRIES.removeLast();
        }
    }

    public static synchronized List<Entry> recent() {
        return new ArrayList<>(ENTRIES);
    }

    static synchronized void clear() {
        ENTRIES.clear();
    }
}
