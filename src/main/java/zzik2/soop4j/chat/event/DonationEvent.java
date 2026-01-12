package zzik2.soop4j.chat.event;

public class DonationEvent extends BaseChatEvent {

    private final DonationType type;
    private final String to;
    private final String from;
    private final String fromUsername;
    private final int amount;
    private final int fanClubOrdinal;

    public DonationEvent(DonationType type, String to, String from, String fromUsername, int amount,
            int fanClubOrdinal) {
        super();
        this.type = type;
        this.to = to;
        this.from = from;
        this.fromUsername = fromUsername;
        this.amount = amount;
        this.fanClubOrdinal = fanClubOrdinal;
    }

    public DonationType getType() {
        return type;
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

    public int getAmount() {
        return amount;
    }

    public int getFanClubOrdinal() {
        return fanClubOrdinal;
    }

    public boolean isNewFanClub() {
        return fanClubOrdinal > 0;
    }
}
