package zzik2.soop4j.chat.packet;

public enum ChatType {

    PING("0000"),
    CONNECT("0001"),
    ENTER_CHAT_ROOM("0002"),
    EXIT("0004"),
    CHAT("0005"),
    DISCONNECT("0007"),
    ENTER_INFO("0012"),
    TEXT_DONATION("0018"),
    AD_BALLOON_DONATION("0087"),
    SUBSCRIBE("0093"),
    NOTIFICATION("0104"),
    VIDEO_DONATION("0105"),
    EMOTICON("0109"),
    VIEWER("0127");

    private final String code;

    ChatType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static ChatType fromCode(String code) {
        for (ChatType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}
