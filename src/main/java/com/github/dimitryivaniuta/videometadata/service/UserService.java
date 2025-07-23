package com.github.dimitryivaniuta.videometadata.service;

import com.github.dimitryivaniuta.videometadata.domain.entity.User;
import com.github.dimitryivaniuta.videometadata.domain.model.Role;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Set;

/**
 * Reactive API for managing {@link User} entities.
 * <p>
 * All methods are non-blocking and return Reactor types. Business rules such as uniqueness,
 * password encoding and role normalization are enforced here, not in the controller layer.
 */
public interface UserService {

    /**
     * Creates a new user with the provided parameters.
     *
     * @param username   unique username
     * @param email      optional unique email
     * @param rawPassword plaintext password (will be encoded)
     * @param roles      optional roles; when {@code null} or empty, defaults to {@link Role#USER}
     * @return mono emitting the persisted user
     */
    Mono<User> createUser(String username, String email, String rawPassword, Set<Role> roles);

    /**
     * Returns a user by id.
     *
     * @param id user id
     * @return mono with the user or empty if not found
     */
    Mono<User> getById(Long id);

    /**
     * Returns a user by username.
     *
     * @param username username
     * @return mono with the user or empty if not found
     */
    Mono<User> getByUsername(String username);

    /**
     * Updates base profile fields (username/email). Does not change password or roles.
     *
     * @param id       user id
     * @param username new username
     * @param email    new email
     * @return mono with updated user or error if not found/violates constraints
     */
    Mono<User> updateProfile(Long id, String username, String email);

    /**
     * Replaces the complete role set for the user.
     *
     * @param id    user id
     * @param roles new role set
     * @return mono emitting the updated user
     */
    Mono<User> replaceRoles(Long id, Set<Role> roles);

    /**
     * Changes the password to the encoded form of {@code rawPassword}.
     *
     * @param id          user id
     * @param rawPassword plaintext
     * @return mono signalling completion or error
     */
    Mono<Void> changePassword(Long id, String rawPassword);

    /**
     * Enables/disables a user.
     *
     * @param id      user id
     * @param enabled flag
     * @return mono signalling completion or error
     */
    Mono<Void> setEnabled(Long id, boolean enabled);

    /**
     * Locks/unlocks a user.
     *
     * @param id     user id
     * @param locked flag
     * @return mono signalling completion or error
     */
    Mono<Void> setLocked(Long id, boolean locked);

    /**
     * Records the last successful login time.
     *
     * @param id     user id
     * @param moment timestamp (UTC)
     * @return mono signalling completion or error
     */
    Mono<Void> updateLastLogin(Long id, Instant moment);

    /**
     * Deletes a user by id.
     *
     * @param id user id
     * @return mono signalling completion (empty) or error
     */
    Mono<Void> delete(Long id);

    /**
     * Case-insensitive substring search by username.
     *
     * @param fragment part of username
     * @param offset   offset
     * @param limit    max rows
     * @return flux of users
     */
    Flux<User> searchByUsername(String fragment, int offset, int limit);
}
