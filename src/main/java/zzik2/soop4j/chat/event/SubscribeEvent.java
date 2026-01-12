package zzik2.soop4j.chat.event;

public class SubscribeEvent extends BaseChatEvent {

    private final String to;
    private final String from;
    private final String fromUsername;
    private final int monthCount;
    private final int tier;

    public SubscribeEvent(String to, String from, String fromUsername, int monthCount, int tier) {
        super();
        this.to = to;
        this.from = from;
        this.fromUsername = fromUsername;
        this.monthCount = monthCount;
        this.tier = tier;
    }

    public String getTo() {
        return to;
    }

    public String getFrom() {
        return from;
    }

    public String getFromUsername() {
        return fromUsername;
    }

    public int getMonthCount() {
        return monthCount;
    }

    public int getTier() {
        return tier;
    }
}
