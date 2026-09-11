package zzik2.soop4j.api;

import com.google.gson.JsonObject;
import zzik2.soop4j.constant.SoopUrls;
import zzik2.soop4j.http.SoopHttpClient;
import zzik2.soop4j.model.channel.StationInfo;
import zzik2.soop4j.exception.SoopException;
import zzik2.soop4j.internal.Futures;
import zzik2.soop4j.internal.Inputs;

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
        return Futures.await(stationAsync(streamerId));
    }

    /**
     * 채널의 스테이션 정보를 비동기로 조회합니다.
     *
     * @param streamerId 스트리머 ID
     * @return 스테이션 정보를 담은 CompletableFuture
     */
    public CompletableFuture<StationInfo> stationAsync(String streamerId) {
        try {
            String url = baseUrl + "/api/" + Inputs.streamerId(streamerId) + "/station";
            return Futures.map(httpClient.getAsync(url), response -> {
                try {
                    StationInfo result = httpClient.getGson().fromJson(response, StationInfo.class);
                    if (result == null || result.getStation() == null) throw new SoopException("스테이션 정보가 없습니다");
                    return result;
                } catch (SoopException e) {
                    throw e;
                } catch (Exception e) {
                    throw new SoopException("스테이션 응답 파싱 실패: " + streamerId, e);
                }
            });
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}
