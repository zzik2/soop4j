package zzik2.soop4j.model.channel;

import com.google.gson.annotations.SerializedName;

public class Broad {

    @SerializedName("user_id")
    private String userId;

    @SerializedName("broad_no")
    private int broadNo;

    @SerializedName("broad_title")
    private String broadTitle;

    @SerializedName("current_sum_viewer")
    private int currentSumViewer;

    @SerializedName("broad_grade")
    private int broadGrade;

    @SerializedName("is_password")
    private boolean isPassword;

    public String getUserId() {
        return userId;
    }

    public int getBroadNo() {
        return broadNo;
    }

    public String getBroadTitle() {
        return broadTitle;
    }

    public int getCurrentSumViewer() {
        return currentSumViewer;
    }

    public int getBroadGrade() {
        return broadGrade;
    }

    public boolean isPassword() {
        return isPassword;
    }
}
