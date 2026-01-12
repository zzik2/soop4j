package zzik2.soop4j.model.channel;

import com.google.gson.annotations.SerializedName;

public class Station {

    @SerializedName("station_no")
    private int stationNo;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("user_nick")
    private String userNick;

    @SerializedName("station_name")
    private String stationName;

    @SerializedName("station_title")
    private String stationTitle;

    @SerializedName("broad_start")
    private String broadStart;

    private int grade;

    private String jointime;

    @SerializedName("total_broad_time")
    private int totalBroadTime;

    @SerializedName("active_no")
    private int activeNo;

    public int getStationNo() {
        return stationNo;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserNick() {
        return userNick;
    }

    public String getStationName() {
        return stationName;
    }

    public String getStationTitle() {
        return stationTitle;
    }

    public String getBroadStart() {
        return broadStart;
    }

    public int getGrade() {
        return grade;
    }

    public String getJointime() {
        return jointime;
    }

    public int getTotalBroadTime() {
        return totalBroadTime;
    }

    public int getActiveNo() {
        return activeNo;
    }
}
