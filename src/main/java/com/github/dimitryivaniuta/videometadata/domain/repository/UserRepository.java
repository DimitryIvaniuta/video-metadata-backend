package com.github.dimitryivaniuta.videometadata.domain.repository;

import com.github.dimitryivaniuta.videometadata.domain.entity.User;
import com.github.dimitryivaniuta.videometadata.domain.model.Role;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Reactive repository for {@link User} entities.
 * <p>
 * Extends {@link ReactiveCrudRepository} for basic CRUD and {@link UserRepositoryCustom}
 * for fine-grained, tuned operations (e.g. partial updates).
 */
public interface UserRepository extends ReactiveCrudRepository<User, Long>, UserRepositoryCustom {

    /**
     * Finds a user by their unique username.
     *
     * @param username unique username
     * @return a {@link Mono} emitting the user or empty if none found
     */
    Mono<User> findByUsername(String username);

    /**
     * Finds a user by their unique email (if present).
     *
     * @param email unique email
     * @return a {@link Mono} emitting the user or empty if none found
     */
    Mono<User> findByEmail(String email);

    /**
     * Checks if a user with the given username exists.
     *
     * @param username username to check
     * @return {@link Mono} emitting {@code true} if user exists, else {@code false}
     */
    Mono<Boolean> existsByUsername(String username);

    /**
     * Checks if a user with the given email exists.
     *
     * @param email email to check
     * @return {@link Mono} emitting {@code true} if user exists, else {@code false}
     */
    Mono<Boolean> existsByEmail(String email);

    /**
     * Returns all users that have the given role.
     *
     * @param role role to filter by
     * @return {@link Flux} emitting all users that contain the role
     */
    Flux<User> findAllByRolesContains(Role role);
}
