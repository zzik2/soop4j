package zzik2.soop4j.api;

import com.google.gson.JsonObject;
import zzik2.soop4j.constant.SoopUrls;
import zzik2.soop4j.http.SoopHttpClient;
import zzik2.soop4j.model.live.LiveChannel;
import zzik2.soop4j.model.live.LiveDetail;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * SOOP 라이브 관련 API를 제공합니다.
 */
public class SoopLive {

    private static final String THUMBNAIL_URL_FORMAT = "https://liveimg.sooplive.co.kr/m/%s";

    private final SoopHttpClient httpClient;
    private final String baseUrl;

    public SoopLive(SoopHttpClient httpClient) {
        this(httpClient, SoopUrls.LIVE_BASE_URL);
    }

    public SoopLive(SoopHttpClient httpClient, String baseUrl) {
        this.httpClient = httpClient;
        this.baseUrl = baseUrl;
    }

    /**
     * 스트리머의 라이브 상세 정보를 조회합니다.
     *
     * @param streamerId 스트리머 ID
     * @return 라이브 상세 정보
     */
    public LiveDetail detail(String streamerId) {
        Map<String, String> formData = new HashMap<>();
        formData.put("bid", streamerId);
        formData.put("type", "live");
        formData.put("pwd", "");
        formData.put("player_type", "html5");
        formData.put("stream_type", "common");
        formData.put("quality", "HD");
        formData.put("mode", "landing");
        formData.put("from_api", "0");
        formData.put("is_revive", "false");

        String url = baseUrl + "/afreeca/player_live_api.php?bjid=" + streamerId;
        JsonObject response = httpClient.postForm(url, formData);

        JsonObject channelJson = response.getAsJsonObject("CHANNEL");
        LiveChannel channel = httpClient.getGson().fromJson(channelJson, LiveChannel.class);

        return LiveDetail.fromChannel(channel);
    }

    /**
     * 스트리머의 라이브 상세 정보를 비동기로 조회합니다.
     *
     * @param streamerId 스트리머 ID
     * @return 라이브 상세 정보를 담은 CompletableFuture
     */
    public CompletableFuture<LiveDetail> detailAsync(String streamerId) {
        return CompletableFuture.supplyAsync(() -> detail(streamerId));
    }

    /**
     * 스트리머가 현재 방송 중인지 확인합니다.
     *
     * @param streamerId 스트리머 ID
     * @return 방송 중이면 true
     */
    public boolean isOnline(String streamerId) {
        return detail(streamerId).isOnline();
    }

    /**
     * 스트리머가 현재 방송 중인지 비동기로 확인합니다.
     *
     * @param streamerId 스트리머 ID
     * @return 방송 중 여부를 담은 CompletableFuture
     */
    public CompletableFuture<Boolean> isOnlineAsync(String streamerId) {
        return CompletableFuture.supplyAsync(() -> isOnline(streamerId));
    }

    /**
     * 현재 시청자 수를 조회합니다.
     *
     * @param streamerId 스트리머 ID
     * @return 시청자 수 (오프라인이면 0)
     */
    public int getViewerCount(String streamerId) {
        LiveDetail detail = detail(streamerId);
        return detail.isOnline() ? detail.getViewerCount() : 0;
    }

    /**
     * 현재 시청자 수를 비동기로 조회합니다.
     *
     * @param streamerId 스트리머 ID
     * @return 시청자 수를 담은 CompletableFuture
     */
    public CompletableFuture<Integer> getViewerCountAsync(String streamerId) {
        return CompletableFuture.supplyAsync(() -> getViewerCount(streamerId));
    }

    /**
     * 라이브 썸네일 URL을 생성합니다.
     *
     * @param streamerId 스트리머 ID
     * @return 썸네일 이미지 URL
     */
    public String getThumbnailUrl(String streamerId) {
        return String.format(THUMBNAIL_URL_FORMAT, streamerId);
    }
}
