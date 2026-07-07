package com.eventub.userservice.service;

import com.eventub.userservice.dto.UserCreateRequest;
import com.eventub.userservice.dto.UserResponse;
import com.eventub.userservice.dto.UserUpdateRequest;
import com.eventub.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class UserServiceE2ETest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void fullUserLifecycle_createReadUpdateReadDeleteRead() {
        UserCreateRequest createRequest = new UserCreateRequest("Ana Jovic", "ana.e2e@example.com");
        ResponseEntity<UserResponse> createResponse =
                restTemplate.postForEntity("/api/users", createRequest, UserResponse.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        UserResponse created = createResponse.getBody();
        assertThat(created).isNotNull();
        assertThat(created.id()).isNotNull();
        assertThat(created.fullName()).isEqualTo("Ana Jovic");
        assertThat(created.email()).isEqualTo("ana.e2e@example.com");
        assertThat(created.registeredAt()).isNotNull();
        Long id = created.id();

        ResponseEntity<UserResponse> getResponse =
                restTemplate.getForEntity("/api/users/{id}", UserResponse.class, id);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().email()).isEqualTo("ana.e2e@example.com");

        UserUpdateRequest updateRequest = new UserUpdateRequest("Ana Jovic Updated", "ana.updated@example.com");
        ResponseEntity<UserResponse> updateResponse = restTemplate.exchange(
                "/api/users/{id}", HttpMethod.PUT, new HttpEntity<>(updateRequest), UserResponse.class, id);

        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updateResponse.getBody()).isNotNull();
        assertThat(updateResponse.getBody().fullName()).isEqualTo("Ana Jovic Updated");
        assertThat(updateResponse.getBody().email()).isEqualTo("ana.updated@example.com");

        ResponseEntity<UserResponse> confirmUpdateResponse =
                restTemplate.getForEntity("/api/users/{id}", UserResponse.class, id);

        assertThat(confirmUpdateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(confirmUpdateResponse.getBody()).isNotNull();
        assertThat(confirmUpdateResponse.getBody().email()).isEqualTo("ana.updated@example.com");

        restTemplate.delete("/api/users/{id}", id);

        ResponseEntity<String> afterDeleteResponse =
                restTemplate.getForEntity("/api/users/{id}", String.class, id);

        assertThat(afterDeleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
