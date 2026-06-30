package com.eventub.userservice.controller;

import com.eventub.userservice.dto.UserResponse;
import com.eventub.userservice.service.UserService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;

@RestController
@RequestMapping("/users")
public class UserReactiveController {

    private final UserService userService;

    public UserReactiveController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<UserResponse> streamUsers() {
        return Flux.fromIterable(userService.getAllUsers())
                .delayElements(Duration.ofMillis(500));
    }
}
