package zzik2.soop4j.chat.packet;

import java.nio.charset.StandardCharsets;

public final class PacketBuilder {

    private PacketBuilder() {
    }

    public static String buildPacket(ChatType type, String payload) {
        String lengthStr = String.format("%06d", payload.getBytes(StandardCharsets.UTF_8).length);
        return ChatDelimiter.STARTER + type.getCode() + lengthStr + "00" + payload;
    }

    public static String buildConnectPacket() {
        String payload = ChatDelimiter.SEPARATOR.repeat(3) + "16" + ChatDelimiter.SEPARATOR;
        return buildPacket(ChatType.CONNECT, payload);
    }

    public static String buildJoinPacket(String chatNo) {
        String payload = ChatDelimiter.SEPARATOR + chatNo + ChatDelimiter.SEPARATOR.repeat(5);
        return buildPacket(ChatType.ENTER_CHAT_ROOM, payload);
    }

    public static String buildPingPacket() {
        return buildPacket(ChatType.PING, ChatDelimiter.SEPARATOR);
    }
}
