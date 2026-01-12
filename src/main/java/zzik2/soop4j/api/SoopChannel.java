package zzik2.soop4j.api;

import com.google.gson.JsonObject;
import zzik2.soop4j.constant.SoopUrls;
import zzik2.soop4j.http.SoopHttpClient;
import zzik2.soop4j.model.channel.StationInfo;

import java.util.concurrent.CompletableFuture;

/**
 * SOOP 채널 관련 API를 제공합니다.
 */
public class SoopChannel {

    private final SoopHttpClient httpClient;
    private final String baseUrl;

    public SoopChannel(SoopHttpClient httpClient) {
        this(httpClient, SoopUrls.CHANNEL_BASE_URL);
    }

    public SoopChannel(SoopHttpClient httpClient, String baseUrl) {
        this.httpClient = httpClient;
        this.baseUrl = baseUrl;
    }

    /**
     * 채널의 스테이션 정보를 조회합니다.
     *
     * @param streamerId 스트리머 ID
     * @return 스테이션 정보
     */
    public StationInfo station(String streamerId) {
        String url = baseUrl + "/api/" + streamerId + "/station";
        JsonObject response = httpClient.get(url);
        return httpClient.getGson().fromJson(response, StationInfo.class);
    }

    /**
     * 채널의 스테이션 정보를 비동기로 조회합니다.
     *
     * @param streamerId 스트리머 ID
     * @return 스테이션 정보를 담은 CompletableFuture
     */
    public CompletableFuture<StationInfo> stationAsync(String streamerId) {
        return CompletableFuture.supplyAsync(() -> station(streamerId));
    }
}
