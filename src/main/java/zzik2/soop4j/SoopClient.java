package zzik2.soop4j;

import zzik2.soop4j.api.SoopChannel;
import zzik2.soop4j.api.SoopLive;
import zzik2.soop4j.chat.SoopChat;
import zzik2.soop4j.constant.SoopConstants;
import zzik2.soop4j.constant.SoopUrls;
import zzik2.soop4j.http.SoopHttpClient;

/**
 * SOOP API의 메인 클라이언트입니다.
 * 라이브 정보 조회, 채널 정보 조회, 채팅 연결 등의 기능을 제공합니다.
 *
 * <pre>
 * // 기본 사용법
 * SoopClient client = SoopClient.builder().build();
 *
 * // 온라인 여부 확인
 * boolean isOnline = client.live().isOnline("streamerId");
 *
 * // 라이브 정보 조회
 * LiveDetail detail = client.live().detail("streamerId");
 *
 * // 채팅 연결
 * SoopChat chat = client.chat("streamerId")
 *         .addListener(new SoopChatAdapter() {
 *             &#64;Override
 *             public void onChat(ChatEvent event) {
 *                 System.out.println(event.getUsername() + ": " + event.getMessage());
 *             }
 *         });
 * chat.connect();
 * </pre>
 */
public class SoopClient {

    private final SoopHttpClient httpClient;
    private final SoopLive live;
    private final SoopChannel channel;

    private SoopClient(Builder builder) {
        String userAgent = builder.userAgent != null ? builder.userAgent : SoopConstants.DEFAULT_USER_AGENT;
        this.httpClient = new SoopHttpClient(userAgent);

        String liveBaseUrl = builder.liveBaseUrl != null ? builder.liveBaseUrl : SoopUrls.LIVE_BASE_URL;
        String channelBaseUrl = builder.channelBaseUrl != null ? builder.channelBaseUrl : SoopUrls.CHANNEL_BASE_URL;

        this.live = new SoopLive(httpClient, liveBaseUrl);
        this.channel = new SoopChannel(httpClient, channelBaseUrl);
    }

    /**
     * 새로운 SoopClient 빌더를 생성합니다.
     *
     * @return 빌더 인스턴스
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 라이브 API를 반환합니다.
     *
     * @return 라이브 API
     */
    public SoopLive live() {
        return live;
    }

    /**
     * 채널 API를 반환합니다.
     *
     * @return 채널 API
     */
    public SoopChannel channel() {
        return channel;
    }

    /**
     * 채팅 연결 빌더를 생성합니다.
     *
     * @param streamerId 스트리머 ID
     * @return 채팅 빌더
     */
    public SoopChat.Builder chat(String streamerId) {
        return new SoopChat.Builder(streamerId).httpClient(httpClient);
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

        public SoopClient build() {
            return new SoopClient(this);
        }
    }
}
