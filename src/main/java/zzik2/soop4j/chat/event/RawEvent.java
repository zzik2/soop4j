package zzik2.soop4j.chat.event;

public class RawEvent extends BaseChatEvent {

    private final byte[] rawData;

    public RawEvent(byte[] rawData) {
        super();
        this.rawData = rawData;
    }

    public byte[] getRawData() {
        return rawData;
    }
}
