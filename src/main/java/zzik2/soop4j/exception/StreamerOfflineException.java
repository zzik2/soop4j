package zzik2.soop4j.exception;

public class StreamerOfflineException extends SoopException {

    private final String streamerId;

    public StreamerOfflineException(String streamerId) {
        super("스트리머가 오프라인입니다: " + streamerId);
        this.streamerId = streamerId;
    }

    public String getStreamerId() {
        return streamerId;
    }
}
