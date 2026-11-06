package com.chatapp.controller;

import com.chatapp.dto.UpdateProfileRequest;
import com.chatapp.dto.UserResponse;
import com.chatapp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {
        return userService.toResponse(userService.getByEmail(authentication.getName()));
    }

    @PutMapping("/me")
    public UserResponse updateProfile(Authentication authentication, @Valid @RequestBody UpdateProfileRequest request) {
        return userService.updateProfile(authentication.getName(), request);
    }

    @GetMapping("/search")
    public List<UserResponse> search(Authentication authentication, @RequestParam String query) {
        return userService.search(query, authentication.getName());
    }
}
