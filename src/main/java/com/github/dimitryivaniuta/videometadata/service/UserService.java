package com.github.dimitryivaniuta.videometadata.service;

import com.github.dimitryivaniuta.videometadata.domain.entity.User;
import com.github.dimitryivaniuta.videometadata.domain.model.Role;
import com.github.dimitryivaniuta.videometadata.web.dto.user.UserCreateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

/**
 * Service API for user management, exposing both
 * synchronous (blocking) and reactive (non‑blocking) methods.
 */
public interface UserService {

    // ---------- Synchronous (blocking) API ----------

    /**
     * Creates a new user after validating uniqueness of username/email.
     *
     * @param request the user creation payload
     * @return the persisted {@link User} entity
     * @throws IllegalArgumentException if username or email already exists
     */
    User createUser(UserCreateRequest request);

    /**
     * Fetches a user by its unique identifier.
     *
     * @param id the user ID
     * @return an {@link Optional} containing the {@link User} if found
     */
    Optional<User> findById(Long id);

    /**
     * Fetches a user by its username.
     *
     * @param username the unique username
     * @return an {@link Optional} containing the {@link User} if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Lists users in a paginated fashion.
     *
     * @param pageable the pagination and sort parameters
     * @return a {@link Page} of {@link User} entities
     */
    Page<User> listUsers(Pageable pageable);

    /**
     * Updates selected fields of an existing user.
     *
     * @param id          the user ID
     * @param newPassword the new raw password (or null to leave unchanged)
     * @param newRole     the new {@link Role} (or null to leave unchanged)
     * @param enabled     whether to enable/disable the account (or null to leave unchanged)
     * @return the updated {@link User} entity
     * @throws jakarta.persistence.EntityNotFoundException if no user exists with given ID
     */
    User updateUser(Long id,
                    String newPassword,
                    Role newRole,
                    Boolean enabled);

    /**
     * Deletes a user by its ID. Idempotent: no error if user does not exist.
     *
     * @param id the user ID to delete
     */
    void deleteUser(Long id);

    /**
     * Returns all users. Use with caution for large datasets.
     *
     * @return a list of all {@link User} entities
     */
    List<User> findAll();


    // ---------- Reactive (non‑blocking) API ----------

    /**
     * Reactive wrapper for {@link #createUser(UserCreateRequest)}.
     *
     * @param request the user creation payload
     * @return a {@link Mono} emitting the created {@link User}
     */
    Mono<User> createUserMono(UserCreateRequest request);

    /**
     * Reactive wrapper for {@link #findById(Long)}.
     *
     * @param id the user ID
     * @return a {@link Mono} emitting the found {@link User}, or error if not found
     */
    Mono<User> findByIdMono(Long id);

    /**
     * Reactive wrapper for {@link #findByUsername(String)}.
     *
     * @param username the username
     * @return a {@link Mono} emitting the found {@link User}, or error if not found
     */
    Mono<User> findByUsernameMono(String username);

    /**
     * Reactive wrapper for {@link #listUsers(Pageable)}.
     *
     * @param pageable pagination and sort parameters
     * @return a {@link Flux} emitting the page content of {@link User} entities
     */
    Flux<User> listUsersFlux(Pageable pageable);

    /**
     * Reactive wrapper for {@link #updateUser(Long, String, Role, Boolean)}.
     *
     * @param id          the user ID
     * @param newPassword the new raw password (or null)
     * @param newRole     the new role (or null)
     * @param enabled     enabled flag (or null)
     * @return a {@link Mono} emitting the updated {@link User}
     */
    Mono<User> updateUserMono(Long id,
                              String newPassword,
                              Role newRole,
                              Boolean enabled);

    /**
     * Reactive wrapper for {@link #deleteUser(Long)}.
     *
     * @param id the user ID
     * @return a {@link Mono} signaling completion
     */
    Mono<Void> deleteUserMono(Long id);
}
