package com.example.commitmate.core.api;

import com.example.commitmate.user.AuthProvider;
import com.example.commitmate.user.User;
import com.example.commitmate.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AppApiControllerTest {
    @LocalServerPort
    int port;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    private final TestRestTemplate rest = new TestRestTemplate();

    @Test
    void loginSessionCanCreateAndReadGroups() {
        String unique = UUID.randomUUID().toString().substring(0, 8);
        String email = "app-" + unique + "@example.com";
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode("password123"))
                .nickname("app-user-" + unique)
                .provider(AuthProvider.LOCAL)
                .build();
        userRepository.save(user);

        ResponseEntity<Map> login = rest.postForEntity(url("/auth/login"),
                Map.of("email", email, "password", "password123"), Map.class);

        assertThat(login.getStatusCode()).isEqualTo(HttpStatus.OK);
        String cookie = login.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertThat(cookie).contains("JSESSIONID");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(HttpHeaders.COOKIE, cookie.split(";", 2)[0]);
        ResponseEntity<Map> created = rest.exchange(url("/groups"), HttpMethod.POST,
                new HttpEntity<>(Map.of("roomName", "앱 연동 그룹", "description", "통합 테스트"), headers),
                Map.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<Object[]> groups = rest.exchange(url("/groups"), HttpMethod.GET,
                new HttpEntity<>(headers), Object[].class);
        assertThat(groups.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(groups.getBody()).isNotEmpty();
    }

    private String url(String path) {
        return "http://localhost:" + port + "/api/app" + path;
    }
}
