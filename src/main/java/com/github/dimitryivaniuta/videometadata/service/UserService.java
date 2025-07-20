package com.github.dimitryivaniuta.videometadata.service;

import com.github.dimitryivaniuta.videometadata.dto.user.UserCreateRequest;
import com.github.dimitryivaniuta.videometadata.dto.user.UserResponse;
import com.github.dimitryivaniuta.videometadata.dto.user.UserUpdateRequest;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service API for managing application users.
 * <p>
 * All methods return reactive types but perform blocking JPA operations
 * on a boundedElastic scheduler, keeping the WebFlux event loop free.
 */
public interface UserService {

    /**
     * Create and persist a new user account.
     *
     * @param request payload containing username, email, password, and roles
     * @return a {@link Mono} emitting the created {@link UserResponse}, or an error if creation fails
     * @throws IllegalArgumentException if username or email already exist
     */
    Mono<UserResponse> createUser(UserCreateRequest request);

    /**
     * Update an existing user's properties.
     *
     * @param userId  identifier of the user to update
     * @param request payload containing fields to update (email, password, roles, enabled)
     * @return a {@link Mono} emitting the updated {@link UserResponse}, or an error if not found
     * @throws jakarta.persistence.EntityNotFoundException if no user exists with the given ID
     * @throws IllegalArgumentException                     if updated email conflicts with another account
     */
    Mono<UserResponse> updateUser(Long userId, UserUpdateRequest request);

    /**
     * Delete a user by its ID.
     *
     * @param userId identifier of the user to delete
     * @return a {@link Mono} signaling completion, or error if not found
     * @throws jakarta.persistence.EntityNotFoundException if no user exists with the given ID
     */
    Mono<Void> deleteUser(Long userId);

    /**
     * Find a user by its ID.
     *
     * @param userId identifier to look up
     * @return a {@link Mono} emitting the matching {@link UserResponse}, or empty if not found
     */
    Mono<UserResponse> findById(Long userId);

    /**
     * List all users with pagination.
     *
     * @param pageable pagination and sorting parameters
     * @return a {@link Flux} emitting {@link UserResponse} items page by page
     */
    Flux<UserResponse> findAll(Pageable pageable);
}
