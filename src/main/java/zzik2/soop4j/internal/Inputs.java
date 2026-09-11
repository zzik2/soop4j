package zzik2.soop4j.internal;

public final class Inputs {
    private Inputs() {
    }

    public static String streamerId(String value) {
        if (value == null || !value.matches("[A-Za-z0-9_-]+")) {
            throw new IllegalArgumentException("유효한 streamerId가 필요합니다");
        }
        return value;
    }
}
