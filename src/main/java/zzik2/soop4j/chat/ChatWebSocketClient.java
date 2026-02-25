package zzik2.soop4j.chat;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import zzik2.soop4j.chat.packet.PacketBuilder;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

class ChatWebSocketClient extends WebSocketClient {

    private static final Logger logger = Logger.getLogger(ChatWebSocketClient.class.getName());

    private final SoopChat chat;
    private final ScheduledExecutorService executor;

    ChatWebSocketClient(SoopChat chat, URI serverUri) {
        super(serverUri);
        this.chat = chat;
        this.executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "soop4j-ping");
            t.setDaemon(true);
            return t;
        });
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        String connectPacket = PacketBuilder.buildConnectPacket();
        send(connectPacket);
    }

    @Override
    public void onMessage(String message) {
        chat.handleMessage(message);
    }

    @Override
    public void onMessage(ByteBuffer bytes) {
        String message = new String(bytes.array(), StandardCharsets.UTF_8);
        logger.fine("Binary message received: " + message);
        chat.handleMessage(message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        executor.shutdownNow();
        chat.handleClose(reason);
    }

    @Override
    public void onError(Exception ex) {
        logger.log(Level.SEVERE, "WebSocket error", ex);
        chat.handleError(ex);
    }

    void startPing() {
        executor.scheduleAtFixedRate(() -> {
            if (isOpen()) {
                send(PacketBuilder.buildPingPacket());
            }
        }, 60, 60, TimeUnit.SECONDS);
    }

    void sendPacket(String packet) {
        if (isOpen()) {
            send(packet);
        }
    }
}
