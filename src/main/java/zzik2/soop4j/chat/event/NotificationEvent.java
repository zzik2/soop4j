package zzik2.soop4j.chat.event;

public class NotificationEvent extends BaseChatEvent {

    private final String notification;

    public NotificationEvent(String notification) {
        super();
        this.notification = notification;
    }

    public String getNotification() {
        return notification;
    }
}
