package murach.util;

import java.util.List;

/** Immutable description of an email: recipients (To/CC/BCC), subject and body. */
public record Email(List<String> to, List<String> cc, List<String> bcc,
                    String from, String subject, String body, boolean html) {

    public Email {
        to = to == null ? List.of() : List.copyOf(to);
        cc = cc == null ? List.of() : List.copyOf(cc);
        bcc = bcc == null ? List.of() : List.copyOf(bcc);
        if (to.isEmpty() && cc.isEmpty() && bcc.isEmpty()) {
            throw new IllegalArgumentException("Email needs at least one recipient");
        }
    }

    /** Same parameters as the book's sendMail(to, from, subject, body, bodyIsHTML). */
    public static Email of(String to, String from, String subject, String body, boolean html) {
        return new Email(List.of(to), null, null, from, subject, body, html);
    }

    public Email withBcc(List<String> newBcc) {
        return new Email(to, cc, newBcc, from, subject, body, html);
    }
}
