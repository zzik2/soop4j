package zzik2.soop4j.chat.packet;

import zzik2.soop4j.exception.SoopException;
import java.nio.charset.StandardCharsets;

public final class PacketParser {

    private PacketParser() {
    }

    public static ChatType parseMessageType(String packet) {
        if (packet == null || !packet.startsWith(ChatDelimiter.STARTER)) {
            throw new SoopException("잘못된 패킷: STARTER 바이트로 시작하지 않음");
        }
        if (packet.length() < 14) {
            throw new SoopException("잘못된 패킷: 헤더가 잘렸습니다");
        }
        for (int i = 2; i < 14; i++) {
            if (packet.charAt(i) < '0' || packet.charAt(i) > '9') throw new SoopException("잘못된 패킷 헤더");
        }
        int length = Integer.parseInt(packet.substring(6, 12));
        if (packet.substring(14).getBytes(StandardCharsets.UTF_8).length != length) {
            throw new SoopException("패킷 본문 길이가 헤더와 다릅니다");
        }
        String code = packet.substring(2, 6);
        return ChatType.fromCode(code);
    }

    public static String[] splitPayload(String packet) {
        return packet.split(ChatDelimiter.SEPARATOR, -1);
    }

    public static ParsedConnect parseConnect(String packet) {
        String[] parts = requireParts(packet, 3);
        String username = parts.length > 1 ? parts[1] : null;
        String syn = parts.length > 2 ? parts[2] : null;
        return new ParsedConnect(username, syn);
    }

    public static ParsedEnterChatRoom parseEnterChatRoom(String packet) {
        String[] parts = requireParts(packet, 8);
        String streamerId = parts.length > 2 ? parts[2] : null;
        String synAck = parts.length > 7 ? parts[7] : null;
        return new ParsedEnterChatRoom(streamerId, synAck);
    }

    public static ParsedChat parseChat(String packet) {
        String[] parts = requireParts(packet, 7);
        String message = parts.length > 1 ? parts[1] : null;
        String userId = parts.length > 2 ? parts[2] : null;
        String username = parts.length > 6 ? parts[6] : null;
        return new ParsedChat(userId, username, message);
    }

    public static ParsedEmoticon parseEmoticon(String packet) {
        String[] parts = requireParts(packet, 8);
        String emoticonId = parts.length > 3 ? parts[3] : null;
        String userId = parts.length > 6 ? parts[6] : null;
        String username = parts.length > 7 ? parts[7] : null;
        return new ParsedEmoticon(userId, username, emoticonId);
    }

    public static ParsedDonation parseTextDonation(String packet) {
        String[] parts = requireParts(packet, 5);
        String to = parts.length > 1 ? parts[1] : null;
        String from = parts.length > 2 ? parts[2] : null;
        String fromUsername = parts.length > 3 ? parts[3] : null;
        String amount = parts.length > 4 ? parts[4] : null;
        String fanClubOrdinal = parts.length > 5 ? parts[5] : null;
        return new ParsedDonation(to, from, fromUsername, parseIntSafe(amount), parseIntSafe(fanClubOrdinal));
    }

    public static ParsedDonation parseVideoDonation(String packet) {
        String[] parts = requireParts(packet, 6);
        String to = parts.length > 2 ? parts[2] : null;
        String from = parts.length > 3 ? parts[3] : null;
        String fromUsername = parts.length > 4 ? parts[4] : null;
        String amount = parts.length > 5 ? parts[5] : null;
        String fanClubOrdinal = parts.length > 6 ? parts[6] : null;
        return new ParsedDonation(to, from, fromUsername, parseIntSafe(amount), parseIntSafe(fanClubOrdinal));
    }

    public static ParsedDonation parseAdBalloonDonation(String packet) {
        String[] parts = requireParts(packet, 11);
        String to = parts.length > 2 ? parts[2] : null;
        String from = parts.length > 3 ? parts[3] : null;
        String fromUsername = parts.length > 4 ? parts[4] : null;
        String amount = parts.length > 10 ? parts[10] : null;
        String fanClubOrdinal = parts.length > 11 ? parts[11] : null;
        return new ParsedDonation(to, from, fromUsername, parseIntSafe(amount), parseIntSafe(fanClubOrdinal));
    }

    public static ParsedSubscribe parseSubscribe(String packet) {
        String[] parts = requireParts(packet, 5);
        String to = parts.length > 1 ? parts[1] : null;
        String from = parts.length > 2 ? parts[2] : null;
        String fromUsername = parts.length > 3 ? parts[3] : null;
        String monthCount = parts.length > 4 ? parts[4] : null;
        String tier = parts.length > 8 ? parts[8] : null;
        return new ParsedSubscribe(to, from, fromUsername, parseIntSafe(monthCount), parseIntSafe(tier));
    }

    public static ParsedViewer parseViewer(String packet) {
        String[] parts = requireParts(packet, 2);
        if (parts.length > 4) {
            String[] userIds = new String[(parts.length - 1) / 2];
            for (int i = 0; i < userIds.length; i++) {
                userIds[i] = parts[i * 2 + 1];
            }
            return new ParsedViewer(userIds);
        } else {
            String userId = parts.length > 1 ? parts[1] : null;
            return new ParsedViewer(userId != null && !userId.isEmpty() ? new String[] { userId } : new String[0]);
        }
    }

    public static ParsedExit parseExit(String packet) {
        String[] parts = requireParts(packet, 4);
        String userId = parts.length > 2 ? parts[2] : null;
        String username = parts.length > 3 ? parts[3] : null;
        return new ParsedExit(userId, username);
    }

    public static String parseNotification(String packet) {
        String[] parts = requireParts(packet, 5);
        return parts.length > 4 ? parts[4] : null;
    }

    private static int parseIntSafe(String value) {
        if (value == null || value.isEmpty()) {
            return 0;
        }
        try {
            int result = Integer.parseInt(value);
            if (result < 0) throw new NumberFormatException("음수 카운터");
            return result;
        } catch (NumberFormatException e) {
            throw new SoopException("패킷 숫자 필드가 잘못되었습니다", e);
        }
    }

    private static String[] requireParts(String packet, int minimum) {
        parseMessageType(packet);
        String[] parts = splitPayload(packet);
        if (parts.length < minimum) throw new SoopException("패킷 필수 필드가 누락되었습니다");
        return parts;
    }

    public static class ParsedConnect {
        public final String username;
        public final String syn;

        public ParsedConnect(String username, String syn) {
            this.username = username;
            this.syn = syn;
        }
    }

    public static class ParsedEnterChatRoom {
        public final String streamerId;
        public final String synAck;

        public ParsedEnterChatRoom(String streamerId, String synAck) {
            this.streamerId = streamerId;
            this.synAck = synAck;
        }
    }

    public static class ParsedChat {
        public final String userId;
        public final String username;
        public final String message;

        public ParsedChat(String userId, String username, String message) {
            this.userId = userId;
            this.username = username;
            this.message = message;
        }
    }

    public static class ParsedEmoticon {
        public final String userId;
        public final String username;
        public final String emoticonId;

        public ParsedEmoticon(String userId, String username, String emoticonId) {
            this.userId = userId;
            this.username = username;
            this.emoticonId = emoticonId;
        }
    }

    public static class ParsedDonation {
        public final String to;
        public final String from;
        public final String fromUsername;
        public final int amount;
        public final int fanClubOrdinal;

        public ParsedDonation(String to, String from, String fromUsername, int amount, int fanClubOrdinal) {
            this.to = to;
            this.from = from;
            this.fromUsername = fromUsername;
            this.amount = amount;
            this.fanClubOrdinal = fanClubOrdinal;
        }
    }

    public static class ParsedSubscribe {
        public final String to;
        public final String from;
        public final String fromUsername;
        public final int monthCount;
        public final int tier;

        public ParsedSubscribe(String to, String from, String fromUsername, int monthCount, int tier) {
            this.to = to;
            this.from = from;
            this.fromUsername = fromUsername;
            this.monthCount = monthCount;
            this.tier = tier;
        }
    }

    public static class ParsedViewer {
        public final String[] userIds;

        public ParsedViewer(String[] userIds) {
            this.userIds = userIds;
        }
    }

    public static class ParsedExit {
        public final String userId;
        public final String username;

        public ParsedExit(String userId, String username) {
            this.userId = userId;
            this.username = username;
        }
    }
}
