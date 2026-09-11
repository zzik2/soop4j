package zzik2.soop4j.chat;

public class ChatOptions {

    private final boolean autoReconnect;
    private final int reconnectDelayMs;
    private final int maxReconnectAttempts;
    private final boolean trustAllCertificates;
    private final int connectTimeoutMs;

    private ChatOptions(Builder builder) {
        this.autoReconnect = builder.autoReconnect;
        this.reconnectDelayMs = builder.reconnectDelayMs;
        this.maxReconnectAttempts = builder.maxReconnectAttempts;
        this.trustAllCertificates = builder.trustAllCertificates;
        this.connectTimeoutMs = builder.connectTimeoutMs;
    }

    public static ChatOptions defaults() {
        return new Builder().build();
    }

    public boolean isAutoReconnect() {
        return autoReconnect;
    }

    public int getReconnectDelayMs() {
        return reconnectDelayMs;
    }

    public int getMaxReconnectAttempts() {
        return maxReconnectAttempts;
    }

    public boolean isTrustAllCertificates() {
        return trustAllCertificates;
    }

    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public static class Builder {
        private boolean autoReconnect = false;
        private int reconnectDelayMs = 5000;
        private int maxReconnectAttempts = 5;
        private boolean trustAllCertificates = false;
        private int connectTimeoutMs = 20000;

        public Builder connectTimeoutMs(int connectTimeoutMs) {
            this.connectTimeoutMs = connectTimeoutMs;
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

        public ChatOptions build() {
            if (reconnectDelayMs < 0 || maxReconnectAttempts < 0 || connectTimeoutMs <= 0) {
                throw new IllegalArgumentException("재연결 설정은 0 이상, 연결 제한 시간은 0보다 커야 합니다");
            }
            return new ChatOptions(this);
        }
    }
}
