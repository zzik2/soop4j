package zzik2.soop4j.chat;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

interface ChatTransport {
    void connect();
    void sendPacket(String packet);
    void startPing();
    CompletableFuture<Void> closeAsync();

    interface Factory {
        ChatTransport create(SoopChat chat, URI uri, ChatOptions options) throws Exception;
    }
}
