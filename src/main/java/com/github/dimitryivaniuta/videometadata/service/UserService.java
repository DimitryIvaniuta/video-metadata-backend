package com.github.dimitryivaniuta.videometadata.service;

import com.github.dimitryivaniuta.videometadata.domain.entity.User;
import com.github.dimitryivaniuta.videometadata.domain.model.Role;
import com.github.dimitryivaniuta.videometadata.web.dto.UserCreateRequest;
import com.github.dimitryivaniuta.videometadata.web.dto.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

/**
 * User domain service encapsulating user CRUD operations,
 * password encoding, and role management.
 *
 * Provides both synchronous (blocking) methods for internal
 * use within transactional boundaries and reactive wrappers
 * for WebFlux controllers.
 */
public interface UserService {

    // ---------- Synchronous (blocking) API ----------

    /**
     * Creates a new user after validating uniqueness of the username.
     *
     * @param request user creation request
     * @return created user entity
     */
    User createUser(UserCreateRequest request);

    /**
     * Fetches a user by id.
     *
     * @param id user id
     * @return optional containing user if found
     */
    Optional<User> findById(Long id);

    /**
     * Retrieves a user by username.
     *
     * @param username unique username
     * @return optional user
     */
    Optional<User> findByUsername(String username);

    /**
     * Lists users paginated.
     *
     * @param pageable pagination request
     * @return page of users
     */
    Page<User> listUsers(Pageable pageable);

    /**
     * Updates selected fields of a user (only password & role here).
     *
     * @param id user id
     * @param newPassword optional new password plain text (null to ignore)
     * @param newRole optional new role (null to ignore)
     * @param enabled optional enabled flag (null to ignore)
     * @return updated user
     */
    User updateUser(Long id, String newPassword, Role newRole, Boolean enabled);

    /**
     * Deletes user by id (idempotent).
     *
     * @param id user id
     */
    void deleteUser(Long id);

    /**
     * Returns all users (use cautiously for very large datasets).
     *
     * @return list of users
     */
    List<User> findAll();

    // ---------- Reactive convenience wrappers ----------

    Mono<User> createUserMono(UserCreateRequest request);

    Mono<User> findByIdMono(Long id);

    Mono<User> findByUsernameMono(String username);

    Flux<User> listUsersFlux(Pageable pageable);

    Mono<User> updateUserMono(Long id, String newPassword, Role newRole, Boolean enabled);

    Mono<Void> deleteUserMono(Long id);
}
