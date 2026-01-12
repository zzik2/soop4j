package zzik2.soop4j.chat.event;

import java.util.Arrays;
import java.util.List;

public class UnknownEvent extends BaseChatEvent {

    private final String rawPacket;
    private final List<String> parts;

    public UnknownEvent(String rawPacket, String[] parts) {
        super();
        this.rawPacket = rawPacket;
        this.parts = Arrays.asList(parts);
    }

    public String getRawPacket() {
        return rawPacket;
    }

    public List<String> getParts() {
        return parts;
    }
}
