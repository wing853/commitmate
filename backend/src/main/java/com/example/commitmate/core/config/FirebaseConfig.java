package com.example.commitmate.core.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

@Configuration
@Profile("prod")
public class FirebaseConfig {

    @Value("${firebase.project-id:}")
    private String projectId;
    @Value("${firebase.client-email:}")
    private String clientEmail;
    @Value("${firebase.private-key:}")
    private String privateKey;

    @Bean
    @ConditionalOnExpression("'${firebase.project-id:}' != '' and '${firebase.client-email:}' != '' and '${firebase.private-key:}' != ''")
    public FirebaseMessaging firebaseMessaging() throws Exception {
        // .env/Render에는 PEM의 줄바꿈이 "\n" 리터럴로 들어오므로 실제 개행으로 변환한다.
        String pem = privateKey.replace("\\n", "\n");
        GoogleCredentials credentials = ServiceAccountCredentials.fromPkcs8(
                null, clientEmail, pem, null,
                List.of("https://www.googleapis.com/auth/firebase.messaging"));

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .setProjectId(projectId)
                .build();
        FirebaseApp app = FirebaseApp.getApps().isEmpty()
                ? FirebaseApp.initializeApp(options)
                : FirebaseApp.getInstance();
        return FirebaseMessaging.getInstance(app);
    }
}