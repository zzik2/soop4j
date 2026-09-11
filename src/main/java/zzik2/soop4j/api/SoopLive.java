package zzik2.soop4j.api;

import com.google.gson.JsonObject;
import zzik2.soop4j.constant.SoopUrls;
import zzik2.soop4j.exception.SoopException;
import zzik2.soop4j.exception.SoopApiException;
import zzik2.soop4j.internal.Futures;
import zzik2.soop4j.internal.Inputs;
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
    private final SoopChannel channelApi;

    public SoopLive(SoopHttpClient httpClient) {
        this(httpClient, SoopUrls.LIVE_BASE_URL);
    }

    public SoopLive(SoopHttpClient httpClient, String baseUrl) {
        this(httpClient, baseUrl, new SoopChannel(httpClient));
    }

    public SoopLive(SoopHttpClient httpClient, String baseUrl, SoopChannel channelApi) {
        this.httpClient = httpClient;
        this.baseUrl = baseUrl;
        this.channelApi = channelApi;
    }

    /**
     * 스트리머의 라이브 상세 정보를 조회합니다.
     *
     * @param streamerId 스트리머 ID
     * @return 라이브 상세 정보
     */
    public LiveDetail detail(String streamerId) {
        return Futures.await(detailAsync(streamerId));
    }

    private CompletableFuture<LiveChannel> channelAsync(String streamerId) {
        try {
            Inputs.streamerId(streamerId);
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
            return Futures.map(httpClient.postFormAsync(url, formData), response -> {
                try {
                    if (!response.has("CHANNEL") || !response.get("CHANNEL").isJsonObject()
                            || !response.getAsJsonObject("CHANNEL").has("RESULT")
                            || response.getAsJsonObject("CHANNEL").get("RESULT").isJsonNull()) {
                        throw new SoopException("API 응답에 CHANNEL/RESULT 정보가 없습니다: " + streamerId);
                    }
                    LiveChannel channel = httpClient.getGson().fromJson(response.get("CHANNEL"), LiveChannel.class);
                    if (channel.getResult() != 0 && channel.getResult() != 1) {
                        throw new SoopApiException(streamerId, channel.getResult());
                    }
                    return channel;
                } catch (SoopException e) {
                    throw e;
                } catch (Exception e) {
                    throw new SoopException("라이브 응답 파싱 실패: " + streamerId, e);
                }
            });
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * 스트리머의 라이브 상세 정보를 비동기로 조회합니다.
     *
     * @param streamerId 스트리머 ID
     * @return 라이브 상세 정보를 담은 CompletableFuture
     */
    public CompletableFuture<LiveDetail> detailAsync(String streamerId) {
        return Futures.compose(channelAsync(streamerId), channel -> channel.isOnline()
                ? Futures.map(getViewerCountAsync(streamerId), count -> LiveDetail.fromChannel(channel, count))
                : CompletableFuture.completedFuture(LiveDetail.fromChannel(channel)));
    }

    /**
     * 스트리머가 현재 방송 중인지 확인합니다.
     *
     * @param streamerId 스트리머 ID
     * @return 방송 중이면 true
     */
    public boolean isOnline(String streamerId) {
        return Futures.await(isOnlineAsync(streamerId));
    }

    /**
     * 스트리머가 현재 방송 중인지 비동기로 확인합니다.
     *
     * @param streamerId 스트리머 ID
     * @return 방송 중 여부를 담은 CompletableFuture
     */
    public CompletableFuture<Boolean> isOnlineAsync(String streamerId) {
        return Futures.map(channelAsync(streamerId), LiveChannel::isOnline);
    }

    /**
     * 현재 시청자 수를 조회합니다.
     *
     * @param streamerId 스트리머 ID
     * @return 시청자 수 (오프라인이면 0)
     */
    public int getViewerCount(String streamerId) {
        return Futures.await(getViewerCountAsync(streamerId));
    }

    /**
     * 현재 시청자 수를 비동기로 조회합니다.
     *
     * @param streamerId 스트리머 ID
     * @return 시청자 수를 담은 CompletableFuture
     */
    public CompletableFuture<Integer> getViewerCountAsync(String streamerId) {
        return Futures.map(channelApi.stationAsync(streamerId), station -> station.isOnline() ? station.getBroad().getCurrentSumViewer() : 0);
    }

    /**
     * 라이브 썸네일 URL을 생성합니다.
     *
     * @param streamerId 스트리머 ID
     * @return 썸네일 이미지 URL
     */
    public String getThumbnailUrl(String streamerId) {
        return String.format(THUMBNAIL_URL_FORMAT, Inputs.streamerId(streamerId));
    }
}
