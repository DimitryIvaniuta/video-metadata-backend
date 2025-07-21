package com.github.dimitryivaniuta.videometadata.web.controller;

import com.github.dimitryivaniuta.videometadata.web.dto.user.UserCreateRequest;
import com.github.dimitryivaniuta.videometadata.web.dto.user.UserResponse;
import com.github.dimitryivaniuta.videometadata.web.dto.user.UserUpdateRequest;
import com.github.dimitryivaniuta.videometadata.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;


/**
 * REST controller for managing application users.
 * <p>
 * Exposes CRUD operations under <code>/api/users</code>. All endpoints
 * require a valid JWT; only users with the ADMIN role may invoke these methods.
 */
@RestController
@RequestMapping("/api/users")
@Validated
@RequiredArgsConstructor
public class UserController {

    /**
     * The service layer for user operations.
     */
    private final UserService userService;

    /**
     * Create a new user.
     *
     * @param request the {@link UserCreateRequest} containing desired username, email, password, and roles
     * @return a {@link Mono} emitting a {@link ResponseEntity} whose body is the created {@link UserResponse},
     *         with HTTP status 201 (Created) and <code>Location</code> header pointing to the new resource
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<UserResponse>> createUser(
            @Valid @RequestBody UserCreateRequest request) {
        return userService.createUser(request)
                .map(user -> ResponseEntity
                        .created(URI.create("/users/" + user.id()))
                        .body(user)
                );
    }

    /**
     * Update an existing user.
     *
     * @param id      the ID of the user to update
     * @param request the {@link UserUpdateRequest} containing fields to modify
     * @return a {@link Mono} emitting a {@link ResponseEntity} whose body is the updated {@link UserResponse},
     * with HTTP status 200 (OK)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<UserResponse>> updateUser(
            @PathVariable("id") Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        return userService.updateUser(id, request)
                .map(ResponseEntity::ok);
    }

    /**
     * Delete a user by ID.
     *
     * @param id the ID of the user to delete
     * @return a {@link Mono} emitting a {@link ResponseEntity<Void>} with HTTP status 204 (No Content)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<Void>> deleteUser(@PathVariable("id") Long id) {
        return userService.deleteUser(id)
                .thenReturn(ResponseEntity.noContent().build());
    }

    /**
     * Fetch a single user by ID.
     *
     * @param id the ID of the user to retrieve
     * @return a {@link Mono} emitting a {@link ResponseEntity} whose body is the {@link UserResponse},
     *         with HTTP status 200 (OK), or 404 if not found
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<UserResponse>> getById(@PathVariable("id") Long id) {
        return userService.findById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * List all users with pagination.
     *
     * @param page zero-based page index (default 0)
     * @param size page size (default 20)
     * @return a {@link Flux} emitting page contents as {@link UserResponse} objects,
     *         with HTTP status 200 (OK)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Flux<UserResponse> listUsers(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return userService.findAll(pageable);
    }
}
