package zzik2.soop4j.chat;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import zzik2.soop4j.chat.packet.PacketBuilder;

import javax.net.ssl.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.concurrent.*;

class ChatWebSocketClient extends WebSocketClient implements ChatTransport {
    private final SoopChat chat;
    private final Socket ownedSocket;
    private final ScheduledExecutorService executor;
    private final CompletableFuture<Void> closed = new CompletableFuture<>();
    private boolean started;
    private volatile boolean closing;
    private ScheduledFuture<?> ping;

    @SuppressWarnings("deprecation")
    ChatWebSocketClient(SoopChat chat, URI serverUri, ChatOptions options) throws Exception {
        super(serverUri, new Draft_6455(), Collections.singletonMap("Sec-WebSocket-Protocol", "chat"), options.getConnectTimeoutMs());
        this.chat = chat;
        this.ownedSocket = sslContext(options).getSocketFactory().createSocket();
        this.ownedSocket.setSoTimeout(options.getConnectTimeoutMs());
        setSocket(ownedSocket);
        setDaemon(true);
        this.executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "soop4j-ping");
            thread.setDaemon(true);
            return thread;
        });
    }

    @Override
    public synchronized void connect() {
        if (closing) return;
        started = true;
        super.connect();
    }

    @Override
    public void run() {
        try {
            if (!closing) super.run();
        } finally {
            executor.shutdownNow();
            try {
                ownedSocket.close();
            } catch (Exception ignored) {
            }
            closed.complete(null);
        }
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        if (closing) return;
        try {
            ownedSocket.setSoTimeout(0);
            send(PacketBuilder.buildConnectPacket());
        } catch (Exception e) {
            onError(e);
        }
    }

    @Override
    public void onMessage(String message) {
        chat.handleMessage(this, message, message.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void onMessage(ByteBuffer bytes) {
        byte[] raw = new byte[bytes.remaining()];
        bytes.duplicate().get(raw);
        chat.handleMessage(this, new String(raw, StandardCharsets.UTF_8), raw);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        executor.shutdownNow();
        chat.handleClose(this, reason);
    }

    @Override
    public void onError(Exception error) {
        if (!closing) chat.handleError(this, error);
    }

    @Override
    public synchronized void startPing() {
        if (closing || ping != null) return;
        ping = executor.scheduleAtFixedRate(() -> {
            try {
                if (isOpen()) send(PacketBuilder.buildPingPacket());
            } catch (Exception e) {
                onError(e);
            }
        }, 60, 60, TimeUnit.SECONDS);
    }

    @Override
    public void sendPacket(String packet) {
        if (!closing) send(packet);
    }

    @Override
    public synchronized CompletableFuture<Void> closeAsync() {
        if (closing) return closed;
        closing = true;
        executor.shutdownNow();
        try {
            ownedSocket.close();
        } catch (Exception ignored) {
        }
        closeConnection(1000, "Client disconnected");
        if (!started) closed.complete(null);
        return closed;
    }

    private static SSLContext sslContext(ChatOptions options) throws Exception {
        if (!options.isTrustAllCertificates()) return SSLContext.getDefault();
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }

                    public void checkClientTrusted(X509Certificate[] chain, String authType) {
                    }

                    public void checkServerTrusted(X509Certificate[] chain, String authType) {
                    }
                }}, null);
        return context;
    }
}
