package zzik2.soop4j.chat.event;

public class EnterChatRoomEvent extends BaseChatEvent {

    private final String streamerId;
    private final String synAck;

    public EnterChatRoomEvent(String streamerId, String synAck) {
        super();
        this.streamerId = streamerId;
        this.synAck = synAck;
    }

    public String getStreamerId() {
        return streamerId;
    }

    public String getSynAck() {
        return synAck;
    }
}
