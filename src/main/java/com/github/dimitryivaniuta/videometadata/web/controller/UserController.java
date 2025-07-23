package com.github.dimitryivaniuta.videometadata.web.controller;

import com.github.dimitryivaniuta.videometadata.domain.entity.User;
import com.github.dimitryivaniuta.videometadata.domain.model.Role;
import com.github.dimitryivaniuta.videometadata.service.UserService;
import com.github.dimitryivaniuta.videometadata.web.dto.user.SetLastLoginRequest;
import com.github.dimitryivaniuta.videometadata.web.dto.user.UserResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Reactive REST controller exposing CRUD and administrative operations for {@link User}.
 * <p>
 * All endpoints are non-blocking and return Reactor types. DTOs are used to decouple
 * HTTP payloads from the domain model.
 */
@Slf4j
@RestController
@RequestMapping(path = "/api/users", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Validated
public class UserController {

    /** Service layer handling business rules for users. */
    private final UserService userService;

    /* =========================================================
       Create
       ========================================================= */

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<UserResponse> create(@Valid @RequestBody CreateUserRequest body) {
        return userService.createUser(body.username(), body.email(), body.password(), body.roles())
                .map(this::toResponse);
    }

    /* =========================================================
       Read
       ========================================================= */

    @GetMapping("/{id}")
    public Mono<ResponseEntity<UserResponse>> getById(@PathVariable Long id) {
        return userService.getById(id)
                .map(u -> ResponseEntity.ok(UserResponse.from(u)))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-username/{username}")
    public Mono<ResponseEntity<UserResponse>> getByUsername(@PathVariable String username) {
        return userService.getByUsername(username)
                .map(u -> ResponseEntity.ok(UserResponse.from(u)))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public Flux<UserResponse> search(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") @PositiveOrZero int offset,
            @RequestParam(defaultValue = "20") @Positive int limit) {
        return userService.searchByUsername(q, offset, Math.min(limit, 200)) // simple cap
                .map(UserResponse::from);
    }

    /* =========================================================
       Update (profile / roles / password / flags)
       ========================================================= */

    @PatchMapping(path = "/{id}/profile", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<UserResponse> updateProfile(@PathVariable Long id,
                                            @Valid @RequestBody UpdateProfileRequest body) {
        return userService.updateProfile(id, body.username(), body.email())
                .map(this::toResponse);
    }

    @PatchMapping(path = "/{id}/roles", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<UserResponse> replaceRoles(@PathVariable Long id,
                                           @Valid @RequestBody ReplaceRolesRequest body) {
        return userService.replaceRoles(id, body.roles())
                .map(this::toResponse);
    }

    @PatchMapping(path = "/{id}/password", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> changePassword(@PathVariable Long id,
                                     @Valid @RequestBody ChangePasswordRequest body) {
        return userService.changePassword(id, body.password());
    }

    @PatchMapping(path = "/{id}/enabled", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> setEnabled(@PathVariable Long id,
                                 @Valid @RequestBody ToggleFlagRequest body) {
        return userService.setEnabled(id, body.value());
    }

    @PatchMapping(path = "/{id}/locked", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> setLocked(@PathVariable Long id,
                                @Valid @RequestBody ToggleFlagRequest body) {
        return userService.setLocked(id, body.value());
    }

    @PatchMapping(path = "/{id}/last-login", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> setLastLogin(@PathVariable Long id,
                                   @Valid @RequestBody SetLastLoginRequest body) {
        return userService.updateLastLogin(id, body.moment());
    }

    /* =========================================================
       Delete
       ========================================================= */

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable Long id) {
        return userService.delete(id);
    }

    /* =========================================================
       Mapping helpers
       ========================================================= */

    /**
     * Maps a domain {@link User} to an outward-facing DTO.
     *
     * @param u domain entity
     * @return response dto
     */
/*    private UserResponse toResponse(User u) {
        return new UserResponse(
                u.getId(),
                u.getUsername(),
                u.getEmail(),
                u.isEnabled(),
                u.isLocked(),
                u.getRoleSet(),
                u.getCreatedAt(),
                u.getUpdatedAt(),
                u.getLastLoginAt()
        );
    }*/

    /* =========================================================
       DTOs
       ========================================================= */
/*
    @Value
    public static class CreateUserRequest {
        @NotBlank String username;
        @Email String email;
        @NotBlank String password;
        Set<Role> roles;
    }

    @Value
    public static class UpdateProfileRequest {
        @NotBlank String username;
        @Email String email;
    }

    @Value
    public static class ReplaceRolesRequest {
        Set<Role> roles;
    }

    @Value
    public static class ChangePasswordRequest {
        @NotBlank String password;
    }

    @Value
    public static class ToggleFlagRequest {
        boolean value;
    }

    @Value
    public static class SetLastLoginRequest {
        Instant moment;
    }

    @Value
    public static class UserResponse {
        Long id;
        String username;
        String email;
        boolean enabled;
        boolean locked;
        Set<Role> roles;
        Instant createdAt;
        Instant updatedAt;
        Instant lastLoginAt;
    }*/
}
