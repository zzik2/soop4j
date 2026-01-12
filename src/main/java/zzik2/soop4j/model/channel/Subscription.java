package zzik2.soop4j.model.channel;

import com.google.gson.annotations.SerializedName;

public class Subscription {

    private int total;
    private int tier1;
    private int tier2;

    public int getTotal() {
        return total;
    }

    public int getTier1() {
        return tier1;
    }

    public int getTier2() {
        return tier2;
    }
}
