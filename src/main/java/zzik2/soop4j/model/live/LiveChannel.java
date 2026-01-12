package zzik2.soop4j.model.live;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class LiveChannel {

    @SerializedName("RESULT")
    private int result;

    @SerializedName("BJID")
    private String streamerId;

    @SerializedName("BJNICK")
    private String streamerNick;

    @SerializedName("TITLE")
    private String title;

    @SerializedName("BNO")
    private String broadcastNo;

    @SerializedName("CATE")
    private String category;

    @SerializedName("CHATNO")
    private String chatNo;

    @SerializedName("CTUSER")
    private int viewerCount;

    @SerializedName("RESOLUTION")
    private String resolution;

    @SerializedName("BPS")
    private String bps;

    @SerializedName("CHIP")
    private String chatIp;

    @SerializedName("CHPT")
    private String chatPort;

    @SerializedName("CHDOMAIN")
    private String chatDomain;

    @SerializedName("FTK")
    private String ftk;

    @SerializedName("TIER1_NICK")
    private String tier1Nick;

    @SerializedName("TIER2_NICK")
    private String tier2Nick;

    @SerializedName("AUTO_HASHTAGS")
    private List<String> autoHashtags;

    @SerializedName("CATEGORY_TAGS")
    private List<String> categoryTags;

    @SerializedName("HASH_TAGS")
    private List<String> hashTags;

    @SerializedName("VIEWPRESET")
    private List<ViewPresetRaw> viewPresets;

    @SerializedName("geo_cc")
    private String geoCountryCode;

    @SerializedName("geo_rc")
    private String geoRegionCode;

    @SerializedName("acpt_lang")
    private String acceptLanguage;

    @SerializedName("svc_lang")
    private String serviceLanguage;

    public int getResult() {
        return result;
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

    public String getChatIp() {
        return chatIp;
    }

    public String getChatPort() {
        return chatPort;
    }

    public String getChatDomain() {
        return chatDomain;
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

    public List<ViewPresetRaw> getViewPresets() {
        return viewPresets;
    }

    public String getGeoCountryCode() {
        return geoCountryCode;
    }

    public String getGeoRegionCode() {
        return geoRegionCode;
    }

    public String getAcceptLanguage() {
        return acceptLanguage;
    }

    public String getServiceLanguage() {
        return serviceLanguage;
    }

    public boolean isOnline() {
        return result != 0;
    }

    public static class ViewPresetRaw {
        private String label;
        @SerializedName("label_resolution")
        private String labelResolution;
        private String name;
        private int bps;

        public String getLabel() {
            return label;
        }

        public String getLabelResolution() {
            return labelResolution;
        }

        public String getName() {
            return name;
        }

        public int getBps() {
            return bps;
        }
    }
}
