package zzik2.soop4j.chat;

import zzik2.soop4j.api.SoopLive;
import zzik2.soop4j.chat.event.*;
import zzik2.soop4j.chat.packet.*;
import zzik2.soop4j.exception.SoopException;
import zzik2.soop4j.exception.StreamerOfflineException;
import zzik2.soop4j.http.SoopHttpClient;
import zzik2.soop4j.internal.Futures;
import zzik2.soop4j.internal.Inputs;
import zzik2.soop4j.model.live.LiveDetail;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SOOP 채팅 연결, 재연결 및 이벤트 수신을 관리합니다.
 */
public class SoopChat {

    private static final Logger LOGGER = Logger.getLogger(SoopChat.class.getName());
    private enum State {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        ENTERED,
        WAITING_RETRY
    }

    private final Object lock = new Object();
    private final String streamerId;
    private final SoopLive liveApi;
    private final ChatOptions options;
    private final ChatTransport.Factory transportFactory;
    private final List<SoopChatListener> listeners = new CopyOnWriteArrayList<>();

    private volatile State state = State.DISCONNECTED;
    private long generation;
    private int reconnectAttempts;
    private ChatTransport transport;
    private LiveDetail liveDetail;
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> deadline;
    private ScheduledFuture<?> retry;
    private CompletableFuture<LiveDetail> discovery;
    private CompletableFuture<Void> connection;
    private CompletableFuture<Void> closing = CompletableFuture.completedFuture(null);

    SoopChat(String streamerId, SoopHttpClient httpClient, ChatOptions options) {
        this(streamerId, new SoopLive(httpClient), options, ChatWebSocketClient::new);
    }

    SoopChat(String streamerId, SoopLive liveApi, ChatOptions options, ChatTransport.Factory factory) {
        this.streamerId = Inputs.streamerId(streamerId);
        this.liveApi = Objects.requireNonNull(liveApi, "liveApi");
        this.options = Objects.requireNonNull(options, "options");
        this.transportFactory = Objects.requireNonNull(factory, "factory");
    }

    public SoopChat addListener(SoopChatListener listener) {
        listeners.add(Objects.requireNonNull(listener, "listener"));
        return this;
    }

    public SoopChat removeListener(SoopChatListener listener) {
        listeners.remove(listener);
        return this;
    }

    public void connect() {
        Futures.await(connectAsync());
    }

    public CompletableFuture<Void> connectAsync() {
        CompletableFuture<Void> result;
        long token;
        synchronized (lock) {
            if (state != State.DISCONNECTED) {
                return CompletableFuture.failedFuture(new SoopException("이미 연결 중이거나 연결되어 있습니다"));
            }
            scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread thread = new Thread(r, "soop4j-chat-timer");
                thread.setDaemon(true);
                return thread;
            });
            state = State.WAITING_RETRY;
            reconnectAttempts = 0;
            token = ++generation;
            result = new CompletableFuture<>();
            connection = result;
        }
        result.whenComplete((value, error) -> {
            if (result.isCancelled()) {
                stop(null, new CancellationException("연결이 취소되었습니다"), result, null);
            }
        });
        startAttempt(token);
        return result;
    }

    public void disconnect() {
        stop(null, new CancellationException("연결이 취소되었습니다"));
    }
    
    public CompletableFuture<Void> disconnectAsync() {
        return stop(null, new CancellationException("연결이 취소되었습니다"));
    }

    public boolean isConnected() {
        State current = state;
        return current == State.CONNECTED || current == State.ENTERED;
    }

    public boolean isEntered() {
        return state == State.ENTERED;
    }

    private void startAttempt(long expected) {
        long token;
        CompletableFuture<LiveDetail> request;
        synchronized (lock) {
            if (generation != expected || state != State.WAITING_RETRY) return;
            state = State.CONNECTING;
            token = ++generation;
            retry = null;
            deadline = scheduler.schedule(() -> failAttempt(token, new SoopException("채팅방 입장 제한 시간 초과")), options.getConnectTimeoutMs(), TimeUnit.MILLISECONDS);
        }

        try {
            request = liveApi.detailAsync(streamerId);
        } catch (Exception e) {
            failAttempt(token, e);
            return;
        }

        synchronized (lock) {
            if (!isCurrent(token)) {
                request.cancel(true);
                return;
            }
            discovery = request;
        }

        request.whenComplete((detail, error) -> {
            if (error != null) {
                failAttempt(token, Futures.unwrap(error));
                return;
            }

            ChatTransport candidate = null;
            try {
                synchronized (lock) {
                    if (!isCurrent(token)) return;
                }

                if (!detail.isOnline()) throw new StreamerOfflineException(streamerId);
                candidate = transportFactory.create(this, buildChatUri(detail), options);
                synchronized (lock) {
                    if (!isCurrent(token)) {
                        candidate.closeAsync();
                        return;
                    }
                    liveDetail = detail;
                    discovery = null;
                    transport = candidate;
                }
                candidate.connect();
            } catch (Exception e) {
                if (candidate != null) candidate.closeAsync();
                failAttempt(token, e);
            }
        });
    }

    private boolean isCurrent(long token) {
        return generation == token && state != State.DISCONNECTED;
    }

    private void cancelTimers() {
        if (deadline != null) deadline.cancel(false);
        if (retry != null) retry.cancel(false);
        deadline = null;
        retry = null;
    }

    private void failAttempt(long token, Throwable error) {
        ChatTransport old;
        CompletableFuture<LiveDetail> request;
        CompletableFuture<Void> failedConnection = null;
        CompletableFuture<Void> cleanup = new CompletableFuture<>();
        boolean terminal;
        synchronized (lock) {
            if (!isCurrent(token)) return;
            long next = ++generation;
            old = transport;
            transport = null;
            closing = CompletableFuture.allOf(closing, cleanup);
            request = discovery;
            discovery = null;
            cancelTimers();
            terminal = !options.isAutoReconnect() || reconnectAttempts >= options.getMaxReconnectAttempts();
            if (terminal) {
                state = State.DISCONNECTED;
                scheduler.shutdownNow();
                failedConnection = connection;
            } else {
                reconnectAttempts++;
                state = State.WAITING_RETRY;
                retry = scheduler.schedule(() -> startAttempt(next), options.getReconnectDelayMs(), TimeUnit.MILLISECONDS);
            }
        }
        if (request != null) request.cancel(true);
        closeTransport(old, cleanup);
        if (terminal) {
            if (failedConnection != null) failedConnection.completeExceptionally(error);
            emit(l -> l.onDisconnect(new DisconnectEvent(streamerId, error.getMessage())));
        }
    }

    private CompletableFuture<Void> stop(String reason, Throwable error) {
        return stop(reason, error, null, null);
    }

    private CompletableFuture<Void> stop(String reason, Throwable error, CompletableFuture<Void> expectedConnection, ChatTransport expectedTransport) {
        ChatTransport old;
        CompletableFuture<LiveDetail> request;
        CompletableFuture<Void> pending;
        CompletableFuture<Void> previousCleanup;
        CompletableFuture<Void> result = new CompletableFuture<>();
        synchronized (lock) {
            if (expectedConnection != null && connection != expectedConnection) return CompletableFuture.completedFuture(null);
            if (expectedTransport != null && transport != expectedTransport) return CompletableFuture.completedFuture(null);
            if (state == State.DISCONNECTED) return closing;
            ++generation;
            state = State.DISCONNECTED;
            old = transport;
            transport = null;
            request = discovery;
            discovery = null;
            pending = connection;
            cancelTimers();
            if (scheduler != null) scheduler.shutdownNow();
            previousCleanup = closing;
            closing = result;
        }
        if (request != null) request.cancel(true);
        CompletableFuture<Void> cleanup = new CompletableFuture<>();
        closeTransport(old, cleanup);
        CompletableFuture.allOf(previousCleanup, cleanup).whenComplete((value, failure) -> {
            if (failure == null) result.complete(null);
            else result.completeExceptionally(failure);
        });
        if (pending != null) pending.completeExceptionally(error);
        emit(l -> l.onDisconnect(new DisconnectEvent(streamerId, reason)));
        return result;
    }

    private static void closeTransport(ChatTransport transport, CompletableFuture<Void> result) {
        if (transport == null) {
            result.complete(null);
            return;
        }
        try {
            transport.closeAsync().whenComplete((value, error) -> {
                if (error == null) result.complete(null);
                else result.completeExceptionally(error);
            });
        } catch (Exception e) {
            result.completeExceptionally(e);
        }
    }

    void handleClose(ChatTransport source, String reason) {
        long token;
        synchronized (lock) {
            if (transport != source || state == State.DISCONNECTED) return;
            token = generation;
        }
        failAttempt(token, new SoopException(reason == null || reason.isEmpty() ? "WebSocket 연결 종료" : reason));
    }

    void handleError(ChatTransport source, Exception error) {
        synchronized (lock) {
            if (transport != source || state == State.DISCONNECTED) return;
        }
        reportError(error);
        long token;
        synchronized (lock) {
            if (transport != source || state == State.DISCONNECTED) return;
            token = generation;
        }
        failAttempt(token, error);
    }

    void handleMessage(ChatTransport source, String packet, byte[] raw) {
        synchronized (lock) {
            if (transport != source || state == State.DISCONNECTED) return;
        }
        emit(l -> l.onRaw(new RawEvent(raw)));
        try {
            ChatType type = PacketParser.parseMessageType(packet);
            Consumer<SoopChatListener> event;
            CompletableFuture<Void> entered = null;
            synchronized (lock) {
                if (transport != source || state == State.DISCONNECTED) return;
                if (type == null) {
                    event = l -> l.onUnknown(new UnknownEvent(packet, PacketParser.splitPayload(packet)));
                } else switch (type) {
                    case CONNECT: {
                        if (state != State.CONNECTING) return;
                        PacketParser.ParsedConnect p = PacketParser.parseConnect(packet);
                        state = State.CONNECTED;
                        source.sendPacket(PacketBuilder.buildJoinPacket(liveDetail.getChatNo()));
                        event = l -> l.onConnect(new ConnectEvent(streamerId, p.username, p.syn));
                        break;
                    }
                    case ENTER_CHAT_ROOM: {
                        if (state != State.CONNECTED) throw new SoopException("연결 확인 전 입장 응답");
                        PacketParser.ParsedEnterChatRoom p = PacketParser.parseEnterChatRoom(packet);
                        state = State.ENTERED;
                        reconnectAttempts = 0;
                        if (deadline != null) deadline.cancel(false);
                        deadline = null;
                        source.startPing();
                        entered = connection;
                        event = l -> l.onEnterChatRoom(new EnterChatRoomEvent(p.streamerId, p.synAck));
                        break;
                    }
                    case CHAT: {
                        PacketParser.ParsedChat p = PacketParser.parseChat(packet);
                        event = l -> l.onChat(new ChatEvent(p.userId, p.username, p.message));
                        break;
                    }
                    case EMOTICON: {
                        PacketParser.ParsedEmoticon p = PacketParser.parseEmoticon(packet);
                        event = l -> l.onEmoticon(new EmoticonEvent(p.userId, p.username, p.emoticonId));
                        break;
                    }
                    case TEXT_DONATION:
                    case VIDEO_DONATION:
                    case AD_BALLOON_DONATION: {
                        PacketParser.ParsedDonation p = type == ChatType.TEXT_DONATION ? PacketParser.parseTextDonation(packet)
                                : type == ChatType.VIDEO_DONATION ? PacketParser.parseVideoDonation(packet)
                                : PacketParser.parseAdBalloonDonation(packet);
                        DonationType donation = type == ChatType.TEXT_DONATION ? DonationType.TEXT
                                : type == ChatType.VIDEO_DONATION ? DonationType.VIDEO : DonationType.AD_BALLOON;
                        event = l -> l.onDonation(new DonationEvent(donation, p.to, p.from, p.fromUsername, p.amount, p.fanClubOrdinal));
                        break;
                    }
                    case SUBSCRIBE: {
                        PacketParser.ParsedSubscribe p = PacketParser.parseSubscribe(packet);
                        event = l -> l.onSubscribe(new SubscribeEvent(p.to, p.from, p.fromUsername, p.monthCount, p.tier));
                        break;
                    }
                    case VIEWER: {
                        PacketParser.ParsedViewer p = PacketParser.parseViewer(packet);
                        event = l -> l.onViewerJoin(new ViewerEvent(Arrays.asList(p.userIds)));
                        break;
                    }
                    case EXIT: {
                        PacketParser.ParsedExit p = PacketParser.parseExit(packet);
                        event = l -> l.onViewerExit(new ExitEvent(p.userId, p.username));
                        break;
                    }
                    case NOTIFICATION:
                        String notice = PacketParser.parseNotification(packet);
                        event = l -> l.onNotification(new NotificationEvent(notice));
                        break;
                    case DISCONNECT:
                        event = null;
                        break;
                    default:
                        event = l -> l.onUnknown(new UnknownEvent(packet, PacketParser.splitPayload(packet)));
                }
            }
            if (event == null) {
                stop("서버에서 채팅 연결을 종료했습니다", new SoopException("서버에서 연결 종료"), null, source);
                return;
            }
            if (entered != null) entered.complete(null);
            emit(event);
        } catch (Exception e) {
            reportError(e);
        }
    }

    private URI buildChatUri(LiveDetail detail) {
        try {
            if (detail.getChatDomain() == null || detail.getChatDomain().isBlank() || detail.getChatNo() == null || detail.getChatNo().isBlank()) {
                throw new IllegalArgumentException("채팅 서버 또는 채팅방 정보 누락");
            }
            int port = Math.addExact(Integer.parseInt(detail.getChatPort()), 1);
            if (port < 1 || port > 65535) throw new IllegalArgumentException("잘못된 채팅 포트");
            return new URI("wss", null, detail.getChatDomain().toLowerCase(Locale.ROOT), port, "/Websocket/" + streamerId, null, null);
        } catch (Exception e) {
            throw new SoopException("채팅 서버 정보가 잘못되었습니다", e);
        }
    }

    private void emit(Consumer<SoopChatListener> callback) {
        for (SoopChatListener listener : listeners) {
            try {
                callback.accept(listener);
            } catch (Exception e) {
                reportError(e);
            }
        }
    }

    private void reportError(Exception error) {
        LOGGER.log(Level.WARNING, "SOOP 채팅 처리 오류", error);
        for (SoopChatListener listener : listeners) {
            try {
                listener.onError(error);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "onError 리스너 오류", e);
            }
        }
    }

    public static class Builder {
        private final String streamerId;
        private SoopHttpClient httpClient;
        private SoopLive liveApi;
        private ChatOptions.Builder options = new ChatOptions.Builder();

        public Builder(String streamerId) {
            this.streamerId = Inputs.streamerId(streamerId);
        }

        public Builder httpClient(SoopHttpClient value) {
            httpClient = value;
            liveApi = null;
            return this;
        }

        public Builder liveApi(SoopLive value) {
            liveApi = Objects.requireNonNull(value);
            return this;
        }

        public Builder autoReconnect(boolean value) {
            options.autoReconnect(value);
            return this;
        }

        public Builder reconnectDelayMs(int value) {
            options.reconnectDelayMs(value);
            return this;
        }

        public Builder maxReconnectAttempts(int value) {
            options.maxReconnectAttempts(value);
            return this;
        }

        public Builder trustAllCertificates(boolean value) {
            options.trustAllCertificates(value);
            return this;
        }

        public Builder connectTimeoutMs(int value) {
            options.connectTimeoutMs(value);
            return this;
        }

        public Builder options(ChatOptions value) {
            options = new ChatOptions.Builder().autoReconnect(value.isAutoReconnect())
                    .reconnectDelayMs(value.getReconnectDelayMs()).maxReconnectAttempts(value.getMaxReconnectAttempts())
                    .trustAllCertificates(value.isTrustAllCertificates()).connectTimeoutMs(value.getConnectTimeoutMs());
            return this;
        }

        public SoopChat build() {
            SoopLive api = liveApi != null ? liveApi : new SoopLive(httpClient != null ? httpClient : new SoopHttpClient());
            return new SoopChat(streamerId, api, options.build(), ChatWebSocketClient::new);
        }
    }
}
