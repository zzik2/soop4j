package zzik2.soop4j;

import zzik2.soop4j.chat.SoopChat;
import zzik2.soop4j.chat.SoopChatAdapter;
import zzik2.soop4j.chat.event.*;
import zzik2.soop4j.exception.StreamerOfflineException;
import zzik2.soop4j.model.channel.StationInfo;
import zzik2.soop4j.model.live.LiveDetail;

public class Soop4JTest {

    private static final String STREAMER_ID = "ecvhao";

    public static void main(String[] args) {
        SoopClient client = SoopClient.builder().build();

        System.out.println("=".repeat(50));
        System.out.println("Soop4J 라이브러리 테스트");
        System.out.println("=".repeat(50));

        testChannelApi(client);
        testLiveApi(client);
        testChatConnection(client);
    }

    private static void testChannelApi(SoopClient client) {
        System.out.println("\n[채널 API 테스트]");
        try {
            StationInfo station = client.channel().station(STREAMER_ID);
            System.out.println("스테이션 이름: " + station.getStation().getStationName());
            System.out.println("유저 닉네임: " + station.getStation().getUserNick());
            System.out.println("프로필 이미지: " + station.getProfileImage());
            System.out.println("파트너 BJ: " + station.isPartnerBj());
            System.out.println("현재 온라인: " + station.isOnline());

            if (station.getBroad() != null) {
                System.out.println("방송 제목: " + station.getBroad().getBroadTitle());
                System.out.println("시청자 수: " + station.getBroad().getCurrentSumViewer());
            }
        } catch (Exception e) {
            System.out.println("채널 API 오류: " + e.getMessage());
        }
    }

    private static void testLiveApi(SoopClient client) {
        System.out.println("\n[라이브 API 테스트]");
        try {
            boolean isOnline = client.live().isOnline(STREAMER_ID);
            System.out.println("온라인 여부: " + isOnline);

            if (isOnline) {
                LiveDetail detail = client.live().detail(STREAMER_ID);
                System.out.println("스트리머 ID: " + detail.getStreamerId());
                System.out.println("스트리머 닉네임: " + detail.getStreamerNick());
                System.out.println("방송 제목: " + detail.getTitle());
                System.out.println("카테고리: " + detail.getCategory());
                System.out.println("시청자 수: " + detail.getViewerCount());
                System.out.println("해상도: " + detail.getResolution());
                System.out.println("해시태그: " + detail.getHashTags());

                int viewerCount = client.live().getViewerCount(STREAMER_ID);
                System.out.println("시청자 수 (별도 조회): " + viewerCount);
            }

            String thumbnailUrl = client.live().getThumbnailUrl(STREAMER_ID);
            System.out.println("썸네일 URL: " + thumbnailUrl);
        } catch (Exception e) {
            System.out.println("라이브 API 오류: " + e.getMessage());
        }
    }

    private static void testChatConnection(SoopClient client) {
        System.out.println("\n[채팅 연결 테스트]");

        try {
            SoopChat chat = client.chat(STREAMER_ID)
                    .autoReconnect(true)
                    .build();

            chat.addListener(new SoopChatAdapter() {
                @Override
                public void onConnect(ConnectEvent event) {
                    System.out.println("[연결] " + event.getStreamerId() + " 채팅 서버 연결됨");
                }

                @Override
                public void onEnterChatRoom(EnterChatRoomEvent event) {
                    System.out.println("[입장] 채팅방 입장 완료");
                }

                @Override
                public void onChat(ChatEvent event) {
                    System.out.printf("[채팅] %s(%s): %s%n", event.getUsername(), event.getUserId(), event.getMessage());
                }

                @Override
                public void onEmoticon(EmoticonEvent event) {
                    System.out.printf("[이모티콘] %s: %s%n", event.getUsername(), event.getEmoticonId());
                }

                @Override
                public void onDonation(DonationEvent event) {
                    System.out.printf("[후원] %s님이 %d개 후원 (타입: %s)%n", event.getFromUsername(), event.getAmount(), event.getType());
                    if (event.isNewFanClub()) {
                        System.out.printf("       %d번째 팬클럽 가입!%n", event.getFanClubOrdinal());
                    }
                }

                @Override
                public void onSubscribe(SubscribeEvent event) {
                    System.out.printf("[구독] %s님이 %d개월 구독 (티어 %d)%n", event.getFromUsername(), event.getMonthCount(), event.getTier());
                }

                @Override
                public void onViewerJoin(ViewerEvent event) {
                    if (event.isSingleViewer()) {
                        System.out.println("[입장] " + event.getUserIds().get(0) + "님 입장");
                    } else {
                        System.out.println("[입장] " + event.getUserIds().size() + "명 정보 수신");
                    }
                }

                @Override
                public void onViewerExit(ExitEvent event) {
                    System.out.printf("[퇴장] %s(%s)님 퇴장%n", event.getUsername(), event.getUserId());
                }

                @Override
                public void onNotification(NotificationEvent event) {
                    System.out.println("[공지] " + event.getNotification().replace("\r\n", " "));
                }

                @Override
                public void onDisconnect(DisconnectEvent event) {
                    System.out.println("[연결해제] " + event.getStreamerId() + " 방송 종료");
                }

                @Override
                public void onUnknown(UnknownEvent event) {
                    System.out.println("[알 수 없음] 파트 개수: " + event.getParts().size());
                }
            });

            System.out.println("채팅 연결 시도 중...");
            chat.connectAsync();
            System.out.println("채팅 연결 성공! 이벤트 수신 대기 중... (종료하려면 Ctrl+C)");

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\n종료 중...");
                chat.disconnect();
            }));

            Thread.currentThread().join();

        } catch (StreamerOfflineException e) {
            System.out.println("스트리머가 오프라인입니다: " + e.getStreamerId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.out.println("채팅 연결 오류: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
