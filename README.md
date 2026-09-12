# Soop4J

SOOP(아프리카TV) 라이브/채널/채팅 API를 Java로 사용할 수 있도록 구성한 비공식 클라이언트 라이브러리입니다.
`SoopClient`를 중심으로 라이브 정보 조회, 채널(스테이션) 정보 조회, 채팅 WebSocket 연결을 제공합니다.

이 프로젝트는 [maro5397/soop](https://github.com/maro5397/soop) 프로젝트에서 영감을 받아 제작되었습니다.

## 요구 사항

- Java 11+

## 의존성

- Gson 2.10.1
- Java-WebSocket 1.6.0

## 빠른 시작

### 설치

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation("kr.zzik2:soop4j:0.0.4")
}
```

### 라이브 정보 조회

```java
SoopClient client = SoopClient.builder().build();

boolean isOnline = client.live().isOnline("streamerId");
LiveDetail detail = client.live().detail("streamerId");
int viewerCount = client.live().getViewerCount("streamerId");
```

### 채널(스테이션) 정보 조회

```java
SoopClient client = SoopClient.builder().build();

StationInfo station = client.channel().station("streamerId");
```

### 채팅 연결

```java
SoopClient client = SoopClient.builder().build();

SoopChat chat = client.chat("streamerId")
        .autoReconnect(true)
        .build();

chat.addListener(new SoopChatAdapter() {
    @Override
    public void onChat(ChatEvent event) {
        System.out.println(event.getUsername() + ": " + event.getMessage());
    }
});

chat.connect();
```

## 프로젝트 구조

```
zzik2.soop4j
├─ api
│  ├─ SoopLive        : 라이브 API
│  └─ SoopChannel     : 채널(스테이션) API
├─ chat
│  ├─ SoopChat        : 채팅 WebSocket 클라이언트
│  ├─ SoopChatListener: 이벤트 리스너 인터페이스
│  ├─ SoopChatAdapter : 리스너 기본 구현
│  ├─ ChatOptions     : 채팅 옵션
│  ├─ event           : 채팅 이벤트 모델
│  └─ packet          : 패킷 생성/파싱 및 메시지 유형
├─ constant          : 기본 URL 및 상수
├─ exception         : API/채팅 예외
├─ http
│  └─ SoopHttpClient  : HTTP 통신/Gson 래퍼
├─ model
│  ├─ channel         : 채널 데이터 모델
│  └─ live            : 라이브 데이터 모델
├─ internal          : 입력 검증 및 비동기 작업 유틸리티
├─ SoopClientOptions : 클라이언트 옵션
└─ SoopClient        : 진입점
```

## 핵심 API

### SoopClient

- `SoopClient.builder()`로 생성
- `live()`로 `SoopLive` 접근
- `channel()`로 `SoopChannel` 접근
- `chat(streamerId)`로 채팅 빌더 생성

### SoopLive

- `detail(streamerId)` : 라이브 상세 정보
- `isOnline(streamerId)` : 방송 중 여부
- `getViewerCount(streamerId)` : 시청자 수
- `detailAsync`, `isOnlineAsync`, `getViewerCountAsync` : 비동기 API
- `getThumbnailUrl(streamerId)` : 썸네일 URL

### SoopChannel

- `station(streamerId)` : 스테이션 정보 조회
- `stationAsync(streamerId)` : 비동기 조회

### SoopChat

- `connect()` / `connectAsync()` : 채팅방 입장 완료까지 대기 / 입장 완료 시 Future 성공
- `disconnect()` : 연결 시도·예약된 재연결을 취소하고 소켓 종료 시작
- `disconnectAsync()` : 소켓 정리 완료 시 Future 성공
- `isConnected()` : 채팅 프로토콜 연결 확인 여부. 입장 전에도 `true`일 수 있음
- `isEntered()` : 채팅방 입장 완료 여부
- `addListener(listener)` / `removeListener(listener)` : 이벤트 리스너 등록 / 제거

## 채팅 이벤트

`SoopChatListener` 또는 `SoopChatAdapter`에서 필요한 메서드만 오버라이드해 사용합니다.
한 이벤트의 리스너는 순차적으로 호출되므로 오래 걸리는 작업은 별도 실행자로 넘기세요.

- `onConnect` : 채팅 프로토콜 연결 확인(입장 전)
- `onEnterChatRoom` : 채팅방 입장 완료
- `onChat` : 일반 채팅 메시지
- `onEmoticon` : 이모티콘 메시지
- `onDonation` : 후원 메시지
- `onSubscribe` : 구독 이벤트
- `onViewerJoin` / `onViewerExit` : 입장/퇴장
- `onNotification` : 알림 이벤트
- `onDisconnect` : 명시적 종료, 서버의 종료 통지 또는 연결·재연결 최종 실패
- `onRaw` : 원본 패킷
- `onUnknown` : 알 수 없는 패킷
- `onError` : WebSocket 통신·패킷 처리·리스너 오류

## 예외 처리

- `StreamerOfflineException` : 스트리머가 오프라인일 때 채팅 연결 시 발생
- `SoopException` : 일반 API/채팅 오류
- `SoopApiException` : `RESULT`가 정상(`1`)/오프라인(`0`) 이외인 라이브 API 응답. `getResultCode()`로 원래 코드를 확인합니다.
- `IllegalArgumentException` : 잘못된 스트리머 ID 또는 유효 범위를 벗어난 옵션

비동기 호출의 실패는 Future로 전달됩니다. `.join()`으로 기다리면 일반적인 작업 실패는 `CompletionException`으로 감싸집니다.
리스너가 던진 예외는 로그와 `onError(Exception)`로 전달되며, 다른 리스너의 호출을 중단하지 않습니다.

## 비동기 사용

`SoopLive`, `SoopChannel`, `SoopChat`은 `CompletableFuture` 기반 비동기 메서드를 제공합니다.

HTTP 비동기 메서드는 `HttpClient.sendAsync()`를 사용합니다. 개별 HTTP 요청은 응답 본문 수신까지 기본 15초로 제한되며,
`SoopClient.builder().requestTimeout(Duration.ofSeconds(10))`으로 변경할 수 있습니다.
동기 호출이 인터럽트되면 요청을 취소하고 인터럽트 상태를 유지합니다.

```java
SoopClient client = SoopClient.builder().build();

client.live().detailAsync("streamerId")
        .whenComplete((detail, error) -> {
            if (error != null) {
                System.err.println(error.getMessage());
                return;
            }
            System.out.println(detail.getTitle());
        });
```

입장 대기 중인 `connectAsync()`의 Future를 취소하면 연결 시도와 예약된 재연결도 중단합니다.
이미 입장을 완료한 연결은 `disconnect()` 또는 `disconnectAsync()`로 종료합니다.

## 옵션 커스터마이징

### 사용자 에이전트/기본 URL/요청 제한 시간

```java
import java.time.Duration;

SoopClient client = SoopClient.builder()
        .userAgent("MyApp/1.0")
        .liveBaseUrl("https://live.sooplive.co.kr")
        .channelBaseUrl("https://chapi.sooplive.co.kr")
        .requestTimeout(Duration.ofSeconds(15))
        .build();
```

위 URL은 현재 기본값입니다. `requestTimeout`은 1ms 이상이어야 합니다.
`SoopClientOptions`는 `SoopClient.builder().options(options)`로 적용할 수 있으며,
요청 제한 시간은 클라이언트 빌더의 `requestTimeout(...)`으로 지정합니다.

### 채팅 재연결 옵션

```java
SoopClient client = SoopClient.builder().build();

ChatOptions options = new ChatOptions.Builder()
        .autoReconnect(true)
        .reconnectDelayMs(5000)
        .maxReconnectAttempts(5)
        .connectTimeoutMs(20000)
        .build();

SoopChat chat = client.chat("streamerId").options(options).build();
```

입장 제한 시간은 API 조회와 WebSocket/채팅방 핸드셰이크를 포함합니다.
`maxReconnectAttempts`는 최초 시도 이후 허용하는 추가 시도 횟수이며, 재입장 성공 시 초기화됩니다.
사용자가 명시적으로 종료하면 재시도하지 않습니다. `liveBaseUrl` 설정은 채팅 연결 준비에도 적용됩니다.

| 옵션 | 기본값 | 범위/의미 |
| --- | --- | --- |
| `autoReconnect` | `false` | 연결 실패 또는 단절 후 자동 재시도 |
| `reconnectDelayMs` | `5000` | 재시도 간 대기 시간(ms), 0 이상 |
| `maxReconnectAttempts` | `5` | 최초 시도 이후 추가 시도 횟수, 0 이상 |
| `connectTimeoutMs` | `20000` | 시도별 입장 제한 시간(ms), 0보다 큼 |
| `trustAllCertificates` | `false` | `true`이면 TLS 인증서 신뢰 검증 생략 |

### 채팅 TLS 인증서 검증 옵션

```java
SoopClient client = SoopClient.builder().build();

ChatOptions options = new ChatOptions.Builder()
        .trustAllCertificates(true)
        .build();

SoopChat chat = client.chat("streamerId").options(options).build();
```

인증서 신뢰 검증을 생략해야 하는 테스트 환경에서만 명시적으로 활성화하세요. 기본 설정은 검증을 수행합니다.

## 빌드/테스트

```bash
./gradlew build
./gradlew test
```

## 라이선스

[MIT License](LICENSE)를 따릅니다.
