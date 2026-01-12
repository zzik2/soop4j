package zzik2.soop4j.model.live;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 라이브 방송 상세 정보를 담는 불변 객체입니다.
 */
public class LiveDetail {

    private final String streamerId;
    private final String streamerNick;
    private final String title;
    private final String broadcastNo;
    private final String category;
    private final String chatNo;
    private final int viewerCount;
    private final String resolution;
    private final String bps;
    private final String chatDomain;
    private final String chatPort;
    private final String ftk;
    private final String tier1Nick;
    private final String tier2Nick;
    private final List<String> autoHashtags;
    private final List<String> categoryTags;
    private final List<String> hashTags;
    private final List<ViewPreset> viewPresets;
    private final String geoCountryCode;
    private final String geoRegionCode;
    private final boolean online;

    private LiveDetail(Builder builder) {
        this.streamerId = builder.streamerId;
        this.streamerNick = builder.streamerNick;
        this.title = builder.title;
        this.broadcastNo = builder.broadcastNo;
        this.category = builder.category;
        this.chatNo = builder.chatNo;
        this.viewerCount = builder.viewerCount;
        this.resolution = builder.resolution;
        this.bps = builder.bps;
        this.chatDomain = builder.chatDomain;
        this.chatPort = builder.chatPort;
        this.ftk = builder.ftk;
        this.tier1Nick = builder.tier1Nick;
        this.tier2Nick = builder.tier2Nick;
        this.autoHashtags = builder.autoHashtags != null ? Collections.unmodifiableList(builder.autoHashtags)
                : Collections.emptyList();
        this.categoryTags = builder.categoryTags != null ? Collections.unmodifiableList(builder.categoryTags)
                : Collections.emptyList();
        this.hashTags = builder.hashTags != null ? Collections.unmodifiableList(builder.hashTags)
                : Collections.emptyList();
        this.viewPresets = builder.viewPresets != null ? Collections.unmodifiableList(builder.viewPresets)
                : Collections.emptyList();
        this.geoCountryCode = builder.geoCountryCode;
        this.geoRegionCode = builder.geoRegionCode;
        this.online = builder.online;
    }

    public static LiveDetail fromChannel(LiveChannel channel) {
        List<ViewPreset> presets = channel.getViewPresets() != null
                ? channel.getViewPresets().stream()
                        .map(r -> new ViewPreset(r.getLabel(), r.getLabelResolution(), r.getName(), r.getBps()))
                        .collect(Collectors.toList())
                : Collections.emptyList();

        return new Builder()
                .streamerId(channel.getStreamerId())
                .streamerNick(channel.getStreamerNick())
                .title(channel.getTitle())
                .broadcastNo(channel.getBroadcastNo())
                .category(channel.getCategory())
                .chatNo(channel.getChatNo())
                .viewerCount(channel.getViewerCount())
                .resolution(channel.getResolution())
                .bps(channel.getBps())
                .chatDomain(channel.getChatDomain())
                .chatPort(channel.getChatPort())
                .ftk(channel.getFtk())
                .tier1Nick(channel.getTier1Nick())
                .tier2Nick(channel.getTier2Nick())
                .autoHashtags(channel.getAutoHashtags())
                .categoryTags(channel.getCategoryTags())
                .hashTags(channel.getHashTags())
                .viewPresets(presets)
                .geoCountryCode(channel.getGeoCountryCode())
                .geoRegionCode(channel.getGeoRegionCode())
                .online(channel.isOnline())
                .build();
    }

    public String getStreamerId() {
        return streamerId;
    }

    public String getStreamerNick() {
        return streamerNick;
    }

    public String getTitle() {
        return title;
    }

    public String getBroadcastNo() {
        return broadcastNo;
    }

    public String getCategory() {
        return category;
    }

    public String getChatNo() {
        return chatNo;
    }

    public int getViewerCount() {
        return viewerCount;
    }

    public String getResolution() {
        return resolution;
    }

    public String getBps() {
        return bps;
    }

    public String getChatDomain() {
        return chatDomain;
    }

    public String getChatPort() {
        return chatPort;
    }

    public String getFtk() {
        return ftk;
    }

    public String getTier1Nick() {
        return tier1Nick;
    }

    public String getTier2Nick() {
        return tier2Nick;
    }

    public List<String> getAutoHashtags() {
        return autoHashtags;
    }

    public List<String> getCategoryTags() {
        return categoryTags;
    }

    public List<String> getHashTags() {
        return hashTags;
    }

    public List<ViewPreset> getViewPresets() {
        return viewPresets;
    }

    public String getGeoCountryCode() {
        return geoCountryCode;
    }

    public String getGeoRegionCode() {
        return geoRegionCode;
    }

    public boolean isOnline() {
        return online;
    }

    public static class Builder {
        private String streamerId;
        private String streamerNick;
        private String title;
        private String broadcastNo;
        private String category;
        private String chatNo;
        private int viewerCount;
        private String resolution;
        private String bps;
        private String chatDomain;
        private String chatPort;
        private String ftk;
        private String tier1Nick;
        private String tier2Nick;
        private List<String> autoHashtags;
        private List<String> categoryTags;
        private List<String> hashTags;
        private List<ViewPreset> viewPresets;
        private String geoCountryCode;
        private String geoRegionCode;
        private boolean online;

        public Builder streamerId(String streamerId) {
            this.streamerId = streamerId;
            return this;
        }

        public Builder streamerNick(String streamerNick) {
            this.streamerNick = streamerNick;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder broadcastNo(String broadcastNo) {
            this.broadcastNo = broadcastNo;
            return this;
        }

        public Builder category(String category) {
            this.category = category;
            return this;
        }

        public Builder chatNo(String chatNo) {
            this.chatNo = chatNo;
            return this;
        }

        public Builder viewerCount(int viewerCount) {
            this.viewerCount = viewerCount;
            return this;
        }

        public Builder resolution(String resolution) {
            this.resolution = resolution;
            return this;
        }

        public Builder bps(String bps) {
            this.bps = bps;
            return this;
        }

        public Builder chatDomain(String chatDomain) {
            this.chatDomain = chatDomain;
            return this;
        }

        public Builder chatPort(String chatPort) {
            this.chatPort = chatPort;
            return this;
        }

        public Builder ftk(String ftk) {
            this.ftk = ftk;
            return this;
        }

        public Builder tier1Nick(String tier1Nick) {
            this.tier1Nick = tier1Nick;
            return this;
        }

        public Builder tier2Nick(String tier2Nick) {
            this.tier2Nick = tier2Nick;
            return this;
        }

        public Builder autoHashtags(List<String> autoHashtags) {
            this.autoHashtags = autoHashtags;
            return this;
        }

        public Builder categoryTags(List<String> categoryTags) {
            this.categoryTags = categoryTags;
            return this;
        }

        public Builder hashTags(List<String> hashTags) {
            this.hashTags = hashTags;
            return this;
        }

        public Builder viewPresets(List<ViewPreset> viewPresets) {
            this.viewPresets = viewPresets;
            return this;
        }

        public Builder geoCountryCode(String geoCountryCode) {
            this.geoCountryCode = geoCountryCode;
            return this;
        }

        public Builder geoRegionCode(String geoRegionCode) {
            this.geoRegionCode = geoRegionCode;
            return this;
        }

        public Builder online(boolean online) {
            this.online = online;
            return this;
        }

        public LiveDetail build() {
            return new LiveDetail(this);
        }
    }
}
