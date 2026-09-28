package murach.util;

/** Read-only description of one delivery service, shown on the Mail Services page. */
public final class MailServiceInfo {

    private final String mode;
    private final String name;
    private final String description;
    private final String settings;
    private final boolean configured;
    private final boolean active;

    MailServiceInfo(MailConfig.Mode mode, String name, String description, String settings,
            boolean configured, boolean active) {
        this.mode = mode.name().toLowerCase();
        this.name = name;
        this.description = description;
        this.settings = settings;
        this.configured = configured;
        this.active = active;
    }

    public String getMode() { return mode; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getSettings() { return settings; }
    public boolean isConfigured() { return configured; }
    public boolean isActive() { return active; }
}
