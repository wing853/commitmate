package com.example.commitmate.user;

import com.example.commitmate.core.errors.ExceptionInput;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@Profile("prod")
public class ProductionSmsSender implements SmsSender {

    private static final URI SEND_URI = URI.create("https://api.solapi.com/messages/v4/send");
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final String apiKey;
    private final String apiSecret;
    private final String senderNumber;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ProductionSmsSender(@Value("${solapi.api-key:}") String apiKey,
                                @Value("${solapi.api-secret:}") String apiSecret,
                                @Value("${solapi.sender-number:}") String senderNumber) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.senderNumber = senderNumber;
    }

    @Override
    public void sendVerificationCode(String phoneNumber, String code) {
        if (apiKey.isBlank() || apiSecret.isBlank() || senderNumber.isBlank()) {
            throw new ExceptionInput("SMS 발송 서비스 설정이 필요합니다.");
        }

        try {
            String body = objectMapper.writeValueAsString(buildRequestBody(phoneNumber, code));

            HttpRequest request = HttpRequest.newBuilder(SEND_URI)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Authorization", buildAuthorizationHeader())
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() / 100 != 2) {
                log.error("[SOLAPI] SMS 발송 실패. status={}, body={}", response.statusCode(), response.body());
                throw new ExceptionInput("인증번호 발송에 실패했습니다. 잠시 후 다시 시도해 주세요.");
            }
        } catch (ExceptionInput e) {
            throw e;
        } catch (Exception e) {
            log.error("[SOLAPI] SMS 발송 중 오류", e);
            throw new ExceptionInput("인증번호 발송에 실패했습니다. 잠시 후 다시 시도해 주세요.");
        }
    }

    private Map<String, Object> buildRequestBody(String phoneNumber, String code) {
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("to", phoneNumber);
        message.put("from", senderNumber);
        message.put("text", "[commitmate] 인증번호 [" + code + "]를 입력해 주세요.");

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("message", message);
        return root;
    }

    private String buildAuthorizationHeader() {
        String date = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String salt = UUID.randomUUID().toString().replace("-", "");
        String signature = hmacSha256(date + salt, apiSecret);

        return "HMAC-SHA256 apiKey=" + apiKey
                + ", date=" + date
                + ", salt=" + salt
                + ", signature=" + signature;
    }

    private String hmacSha256(String data, String secret) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("HMAC 서명 생성에 실패했습니다.", e);
        }
    }
}