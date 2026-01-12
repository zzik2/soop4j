package zzik2.soop4j.chat.event;

public class ChatEvent extends BaseChatEvent {

    private final String userId;
    private final String username;
    private final String message;

    public ChatEvent(String userId, String username, String message) {
        super();
        this.userId = userId;
        this.username = username;
        this.message = message;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getMessage() {
        return message;
    }
}
