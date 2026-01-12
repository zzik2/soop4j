package zzik2.soop4j;

public class SoopClientOptions {

    private final String userAgent;
    private final String liveBaseUrl;
    private final String channelBaseUrl;

    private SoopClientOptions(Builder builder) {
        this.userAgent = builder.userAgent;
        this.liveBaseUrl = builder.liveBaseUrl;
        this.channelBaseUrl = builder.channelBaseUrl;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public String getLiveBaseUrl() {
        return liveBaseUrl;
    }

    public String getChannelBaseUrl() {
        return channelBaseUrl;
    }

    public static class Builder {
        private String userAgent;
        private String liveBaseUrl;
        private String channelBaseUrl;

        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public Builder liveBaseUrl(String liveBaseUrl) {
            this.liveBaseUrl = liveBaseUrl;
            return this;
        }

        public Builder channelBaseUrl(String channelBaseUrl) {
            this.channelBaseUrl = channelBaseUrl;
            return this;
        }

        public SoopClientOptions build() {
            return new SoopClientOptions(this);
        }
    }
}
