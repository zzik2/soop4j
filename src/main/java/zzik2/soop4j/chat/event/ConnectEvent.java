package zzik2.soop4j.chat.event;

public class ConnectEvent extends BaseChatEvent {

    private final String streamerId;
    private final String username;
    private final String syn;

    public ConnectEvent(String streamerId, String username, String syn) {
        super();
        this.streamerId = streamerId;
        this.username = username;
        this.syn = syn;
    }

    public String getStreamerId() {
        return streamerId;
    }

    public String getUsername() {
        return username;
    }

    public String getSyn() {
        return syn;
    }
}
