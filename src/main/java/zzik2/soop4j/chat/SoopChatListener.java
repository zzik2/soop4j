package zzik2.soop4j.chat;

import zzik2.soop4j.chat.event.*;

/**
 * 채팅 이벤트를 수신하기 위한 리스너 인터페이스입니다.
 * 필요한 메서드만 오버라이드하여 사용할 수 있습니다.
 */
public interface SoopChatListener {

    default void onConnect(ConnectEvent event) {
    }

    default void onEnterChatRoom(EnterChatRoomEvent event) {
    }

    default void onChat(ChatEvent event) {
    }

    default void onEmoticon(EmoticonEvent event) {
    }

    default void onDonation(DonationEvent event) {
    }

    default void onSubscribe(SubscribeEvent event) {
    }

    default void onViewerJoin(ViewerEvent event) {
    }

    default void onViewerExit(ExitEvent event) {
    }

    default void onNotification(NotificationEvent event) {
    }

    default void onDisconnect(DisconnectEvent event) {
    }

    default void onRaw(RawEvent event) {
    }

    default void onUnknown(UnknownEvent event) {
    }
}
