package zzik2.soop4j.exception;

public class SoopApiException extends SoopException {
    private final int resultCode;

    public SoopApiException(String streamerId, int resultCode) {
        super("SOOP 라이브 API 오류: " + streamerId + " (RESULT=" + resultCode + ")");
        this.resultCode = resultCode;
    }

    public int getResultCode() {
        return resultCode;
    }
}
