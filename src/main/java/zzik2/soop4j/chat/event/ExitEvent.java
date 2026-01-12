package zzik2.soop4j.chat.event;

public class ExitEvent extends BaseChatEvent {

    private final String userId;
    private final String username;

    public ExitEvent(String userId, String username) {
        super();
        this.userId = userId;
        this.username = username;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }
}
