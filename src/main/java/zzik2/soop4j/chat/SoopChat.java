package zzik2.soop4j.chat;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import zzik2.soop4j.api.SoopLive;
import zzik2.soop4j.chat.event.*;
import zzik2.soop4j.chat.packet.*;
import zzik2.soop4j.exception.SoopException;
import zzik2.soop4j.exception.StreamerOfflineException;
import zzik2.soop4j.http.SoopHttpClient;
import zzik2.soop4j.model.live.LiveDetail;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SOOP 채팅 WebSocket 클라이언트입니다.
 * 실시간 채팅, 후원, 구독 등의 이벤트를 수신할 수 있습니다.
 */
public class SoopChat {

    private static final Logger logger = Logger.getLogger(SoopChat.class.getName());

    private final String streamerId;
    private final SoopHttpClient httpClient;
    private final SoopLive liveApi;
    private final ChatOptions options;
    private final List<SoopChatListener> listeners = new CopyOnWriteArrayList<>();

    private WebSocketClient webSocketClient;
    private LiveDetail liveDetail;
    private ScheduledExecutorService pingScheduler;
    private volatile boolean connected = false;
    private volatile boolean entered = false;
    private int reconnectAttempts = 0;

    SoopChat(String streamerId, SoopHttpClient httpClient, ChatOptions options) {
        this.streamerId = streamerId;
        this.httpClient = httpClient;
        this.liveApi = new SoopLive(httpClient);
        this.options = options;
    }

    public SoopChat addListener(SoopChatListener listener) {
        this.listeners.add(listener);
        return this;
    }

    public SoopChat removeListener(SoopChatListener listener) {
        this.listeners.remove(listener);
        return this;
    }

    public void connect() {
        if (connected) {
            throw new SoopException("이미 연결되어 있습니다");
        }

        liveDetail = liveApi.detail(streamerId);
        if (!liveDetail.isOnline()) {
            throw new StreamerOfflineException(streamerId);
        }

        String chatUrl = buildChatUrl(liveDetail);
        connectWebSocket(chatUrl);
    }

    public CompletableFuture<Void> connectAsync() {
        return CompletableFuture.runAsync(this::connect);
    }

    public void disconnect() {
        if (!connected) {
            return;
        }

        stopPingScheduler();

        if (webSocketClient != null) {
            webSocketClient.close();
            webSocketClient = null;
        }

        connected = false;
        entered = false;

        emitDisconnect(null);
    }

    public CompletableFuture<Void> disconnectAsync() {
        return CompletableFuture.runAsync(this::disconnect);
    }

    public boolean isConnected() {
        return connected;
    }

    public boolean isEntered() {
        return entered;
    }

    private void connectWebSocket(String chatUrl) {
        try {
            URI uri = URI.create(chatUrl);
            webSocketClient = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    String connectPacket = PacketBuilder.buildConnectPacket();
                    send(connectPacket);
                }

                @Override
                public void onMessage(String message) {
                    handleMessage(message);
                }

                @Override
                public void onMessage(ByteBuffer bytes) {
                    String message = new String(bytes.array(), StandardCharsets.UTF_8);
                    logger.fine("Binary message received: " + message);
                    handleMessage(message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    handleClose(reason);
                }

                @Override
                public void onError(Exception ex) {
                    handleError(ex);
                }
            };

            SSLContext sslContext = createSSLContext();
            webSocketClient.setSocketFactory(sslContext.getSocketFactory());
            webSocketClient.addHeader("Sec-WebSocket-Protocol", "chat");
            webSocketClient.connect();

        } catch (Exception e) {
            throw new SoopException("WebSocket 연결 실패", e);
        }
    }

    private void handleMessage(String packet) {
        emitRaw(packet.getBytes(StandardCharsets.UTF_8));

        ChatType messageType = PacketParser.parseMessageType(packet);
        if (messageType == null) {
            emitUnknown(packet);
            return;
        }

        switch (messageType) {
            case CONNECT:
                handleConnect(packet);
                break;
            case ENTER_CHAT_ROOM:
                handleEnterChatRoom(packet);
                break;
            case CHAT:
                handleChat(packet);
                break;
            case EMOTICON:
                handleEmoticon(packet);
                break;
            case TEXT_DONATION:
                handleTextDonation(packet);
                break;
            case VIDEO_DONATION:
                handleVideoDonation(packet);
                break;
            case AD_BALLOON_DONATION:
                handleAdBalloonDonation(packet);
                break;
            case SUBSCRIBE:
                handleSubscribe(packet);
                break;
            case VIEWER:
                handleViewer(packet);
                break;
            case EXIT:
                handleExit(packet);
                break;
            case NOTIFICATION:
                handleNotification(packet);
                break;
            case DISCONNECT:
                disconnect();
                break;
            default:
                emitUnknown(packet);
                break;
        }
    }

    private void handleConnect(String packet) {
        connected = true;
        reconnectAttempts = 0;

        PacketParser.ParsedConnect parsed = PacketParser.parseConnect(packet);
        emitConnect(parsed.username, parsed.syn);

        String joinPacket = PacketBuilder.buildJoinPacket(liveDetail.getChatNo());
        webSocketClient.send(joinPacket);
    }

    private void handleEnterChatRoom(String packet) {
        entered = true;
        startPingScheduler();

        PacketParser.ParsedEnterChatRoom parsed = PacketParser.parseEnterChatRoom(packet);
        emitEnterChatRoom(parsed.streamerId, parsed.synAck);
    }

    private void handleChat(String packet) {
        PacketParser.ParsedChat parsed = PacketParser.parseChat(packet);
        emitChat(parsed.userId, parsed.username, parsed.message);
    }

    private void handleEmoticon(String packet) {
        PacketParser.ParsedEmoticon parsed = PacketParser.parseEmoticon(packet);
        emitEmoticon(parsed.userId, parsed.username, parsed.emoticonId);
    }

    private void handleTextDonation(String packet) {
        PacketParser.ParsedDonation parsed = PacketParser.parseTextDonation(packet);
        emitDonation(DonationType.TEXT, parsed);
    }

    private void handleVideoDonation(String packet) {
        PacketParser.ParsedDonation parsed = PacketParser.parseVideoDonation(packet);
        emitDonation(DonationType.VIDEO, parsed);
    }

    private void handleAdBalloonDonation(String packet) {
        PacketParser.ParsedDonation parsed = PacketParser.parseAdBalloonDonation(packet);
        emitDonation(DonationType.AD_BALLOON, parsed);
    }

    private void handleSubscribe(String packet) {
        PacketParser.ParsedSubscribe parsed = PacketParser.parseSubscribe(packet);
        emitSubscribe(parsed.to, parsed.from, parsed.fromUsername, parsed.monthCount, parsed.tier);
    }

    private void handleViewer(String packet) {
        PacketParser.ParsedViewer parsed = PacketParser.parseViewer(packet);
        emitViewerJoin(Arrays.asList(parsed.userIds));
    }

    private void handleExit(String packet) {
        PacketParser.ParsedExit parsed = PacketParser.parseExit(packet);
        emitViewerExit(parsed.userId, parsed.username);
    }

    private void handleNotification(String packet) {
        String notification = PacketParser.parseNotification(packet);
        emitNotification(notification);
    }

    private void handleClose(String reason) {
        boolean wasConnected = connected;
        stopPingScheduler();
        connected = false;
        entered = false;

        if (wasConnected && options.isAutoReconnect() && reconnectAttempts < options.getMaxReconnectAttempts()) {
            scheduleReconnect();
        } else if (wasConnected) {
            emitDisconnect(reason);
        }
    }

    private void handleError(Exception ex) {
        logger.log(Level.SEVERE, "WebSocket error", ex);
        handleClose("WebSocket 오류: " + ex.getMessage());
    }

    private void scheduleReconnect() {
        reconnectAttempts++;
        CompletableFuture.delayedExecutor(options.getReconnectDelayMs(), TimeUnit.MILLISECONDS)
                .execute(() -> {
                    try {
                        liveDetail = liveApi.detail(streamerId);
                        if (liveDetail.isOnline()) {
                            String chatUrl = buildChatUrl(liveDetail);
                            connectWebSocket(chatUrl);
                        }
                    } catch (Exception e) {
                        handleClose("재연결 실패");
                    }
                });
    }

    private void startPingScheduler() {
        pingScheduler = Executors.newSingleThreadScheduledExecutor();
        pingScheduler.scheduleAtFixedRate(() -> {
            if (webSocketClient != null && connected) {
                webSocketClient.send(PacketBuilder.buildPingPacket());
            }
        }, 60, 60, TimeUnit.SECONDS);
    }

    private void stopPingScheduler() {
        if (pingScheduler != null) {
            pingScheduler.shutdown();
            pingScheduler = null;
        }
    }

    private String buildChatUrl(LiveDetail detail) {
        String domain = detail.getChatDomain().toLowerCase();
        int port = Integer.parseInt(detail.getChatPort()) + 1;
        return String.format("wss://%s:%d/Websocket/%s", domain, port, streamerId);
    }

    private SSLContext createSSLContext() {
        try {
            if (options.isTrustAllCertificates()) {
                TrustManager[] trustAllCerts = new TrustManager[] {
                        new X509TrustManager() {
                            public X509Certificate[] getAcceptedIssuers() {
                                return new X509Certificate[0];
                            }

                            public void checkClientTrusted(X509Certificate[] certs, String authType) {
                            }

                            public void checkServerTrusted(X509Certificate[] certs, String authType) {
                            }
                        }
                };

                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
                return sslContext;
            }
            return SSLContext.getDefault();
        } catch (Exception e) {
            throw new SoopException("SSL 컨텍스트 생성 실패", e);
        }
    }

    private void emitConnect(String username, String syn) {
        ConnectEvent event = new ConnectEvent(streamerId, username, syn);
        listeners.forEach(l -> l.onConnect(event));
    }

    private void emitEnterChatRoom(String streamerId, String synAck) {
        EnterChatRoomEvent event = new EnterChatRoomEvent(streamerId, synAck);
        listeners.forEach(l -> l.onEnterChatRoom(event));
    }

    private void emitChat(String userId, String username, String message) {
        ChatEvent event = new ChatEvent(userId, username, message);
        listeners.forEach(l -> l.onChat(event));
    }

    private void emitEmoticon(String userId, String username, String emoticonId) {
        EmoticonEvent event = new EmoticonEvent(userId, username, emoticonId);
        listeners.forEach(l -> l.onEmoticon(event));
    }

    private void emitDonation(DonationType type, PacketParser.ParsedDonation parsed) {
        DonationEvent event = new DonationEvent(type, parsed.to, parsed.from, parsed.fromUsername, parsed.amount,
                parsed.fanClubOrdinal);
        listeners.forEach(l -> l.onDonation(event));
    }

    private void emitSubscribe(String to, String from, String fromUsername, int monthCount, int tier) {
        SubscribeEvent event = new SubscribeEvent(to, from, fromUsername, monthCount, tier);
        listeners.forEach(l -> l.onSubscribe(event));
    }

    private void emitViewerJoin(List<String> userIds) {
        ViewerEvent event = new ViewerEvent(userIds);
        listeners.forEach(l -> l.onViewerJoin(event));
    }

    private void emitViewerExit(String userId, String username) {
        ExitEvent event = new ExitEvent(userId, username);
        listeners.forEach(l -> l.onViewerExit(event));
    }

    private void emitNotification(String notification) {
        NotificationEvent event = new NotificationEvent(notification);
        listeners.forEach(l -> l.onNotification(event));
    }

    private void emitDisconnect(String reason) {
        DisconnectEvent event = new DisconnectEvent(streamerId, reason);
        listeners.forEach(l -> l.onDisconnect(event));
    }

    private void emitRaw(byte[] rawData) {
        RawEvent event = new RawEvent(rawData);
        listeners.forEach(l -> l.onRaw(event));
    }

    private void emitUnknown(String packet) {
        String[] parts = PacketParser.splitPayload(packet);
        UnknownEvent event = new UnknownEvent(packet, parts);
        listeners.forEach(l -> l.onUnknown(event));
    }

    public static class Builder {
        private final String streamerId;
        private SoopHttpClient httpClient;
        private boolean autoReconnect = false;
        private int reconnectDelayMs = 5000;
        private int maxReconnectAttempts = 5;
        private boolean trustAllCertificates = false;

        public Builder(String streamerId) {
            this.streamerId = streamerId;
        }

        public Builder httpClient(SoopHttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        public Builder autoReconnect(boolean autoReconnect) {
            this.autoReconnect = autoReconnect;
            return this;
        }

        public Builder reconnectDelayMs(int reconnectDelayMs) {
            this.reconnectDelayMs = reconnectDelayMs;
            return this;
        }

        public Builder maxReconnectAttempts(int maxReconnectAttempts) {
            this.maxReconnectAttempts = maxReconnectAttempts;
            return this;
        }

        public Builder trustAllCertificates(boolean trustAllCertificates) {
            this.trustAllCertificates = trustAllCertificates;
            return this;
        }

        public SoopChat build() {
            if (httpClient == null) {
                httpClient = new SoopHttpClient();
            }
            ChatOptions options = new ChatOptions.Builder()
                    .autoReconnect(autoReconnect)
                    .reconnectDelayMs(reconnectDelayMs)
                    .maxReconnectAttempts(maxReconnectAttempts)
                    .trustAllCertificates(trustAllCertificates)
                    .build();
            return new SoopChat(streamerId, httpClient, options);
        }
    }
}
