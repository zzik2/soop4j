package zzik2.soop4j.chat.event;

public class EmoticonEvent extends BaseChatEvent {

    private final String userId;
    private final String username;
    private final String emoticonId;

    public EmoticonEvent(String userId, String username, String emoticonId) {
        super();
        this.userId = userId;
        this.username = username;
        this.emoticonId = emoticonId;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmoticonId() {
        return emoticonId;
    }
}
