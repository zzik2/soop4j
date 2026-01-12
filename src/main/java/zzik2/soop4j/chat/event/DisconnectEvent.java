package zzik2.soop4j.chat.event;

public class DisconnectEvent extends BaseChatEvent {

    private final String streamerId;
    private final String reason;

    public DisconnectEvent(String streamerId) {
        this(streamerId, null);
    }

    public DisconnectEvent(String streamerId, String reason) {
        super();
        this.streamerId = streamerId;
        this.reason = reason;
    }

    public String getStreamerId() {
        return streamerId;
    }

    public String getReason() {
        return reason;
    }
}
