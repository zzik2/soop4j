package zzik2.soop4j.chat.event;

import java.time.Instant;

public abstract class BaseChatEvent {

    private final Instant receivedTime;

    protected BaseChatEvent() {
        this.receivedTime = Instant.now();
    }

    protected BaseChatEvent(Instant receivedTime) {
        this.receivedTime = receivedTime;
    }

    public Instant getReceivedTime() {
        return receivedTime;
    }
}
