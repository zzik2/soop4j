package zzik2.soop4j.chat.packet;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;

public final class PacketBuilder {

    private PacketBuilder() {
    }

    public static String buildPacket(ChatType type, String payload) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(payload, "payload");
        int length = payload.getBytes(StandardCharsets.UTF_8).length;
        if (length > 999999) throw new IllegalArgumentException("패킷 본문이 너무 큽니다");
        String lengthStr = String.format(Locale.ROOT, "%06d", length);
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
