package zzik2.soop4j.chat;

public class ChatOptions {

    private final boolean autoReconnect;
    private final int reconnectDelayMs;
    private final int maxReconnectAttempts;

    private ChatOptions(Builder builder) {
        this.autoReconnect = builder.autoReconnect;
        this.reconnectDelayMs = builder.reconnectDelayMs;
        this.maxReconnectAttempts = builder.maxReconnectAttempts;
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

    public static class Builder {
        private boolean autoReconnect = false;
        private int reconnectDelayMs = 5000;
        private int maxReconnectAttempts = 5;

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

        public ChatOptions build() {
            return new ChatOptions(this);
        }
    }
}
