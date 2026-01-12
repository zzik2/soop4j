package zzik2.soop4j.model.channel;

import com.google.gson.annotations.SerializedName;

/**
 * 채널 스테이션 정보를 담는 모델입니다.
 */
public class StationInfo {

    @SerializedName("profile_image")
    private String profileImage;

    private Station station;

    private Broad broad;

    private Subscription subscription;

    @SerializedName("is_best_bj")
    private boolean isBestBj;

    @SerializedName("is_partner_bj")
    private boolean isPartnerBj;

    @SerializedName("is_favorite")
    private boolean isFavorite;

    @SerializedName("is_subscription")
    private boolean isSubscription;

    private String country;

    @SerializedName("current_timestamp")
    private String currentTimestamp;

    public String getProfileImage() {
        return profileImage;
    }

    public Station getStation() {
        return station;
    }

    public Broad getBroad() {
        return broad;
    }

    public Subscription getSubscription() {
        return subscription;
    }

    public boolean isBestBj() {
        return isBestBj;
    }

    public boolean isPartnerBj() {
        return isPartnerBj;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public boolean isSubscription() {
        return isSubscription;
    }

    public String getCountry() {
        return country;
    }

    public String getCurrentTimestamp() {
        return currentTimestamp;
    }

    public boolean isOnline() {
        return broad != null && broad.getBroadNo() > 0;
    }
}
