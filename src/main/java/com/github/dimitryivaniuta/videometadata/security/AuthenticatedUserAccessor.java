package com.github.dimitryivaniuta.videometadata.security;

import com.github.dimitryivaniuta.videometadata.domain.entity.User;
import com.github.dimitryivaniuta.videometadata.domain.model.Role;
import reactor.core.publisher.Mono;

/**
 * Reactive helper for accessing the currently authenticated principal from the Spring Security context.
 * <p>
 * All methods return {@link Mono} to remain non-blocking and usable in reactive flows.
 */
public interface AuthenticatedUserAccessor {

    /**
     * @return a {@link Mono} emitting the authenticated username, or empty if none.
     */
    Mono<String> currentUsername();

    /**
     * Shortcut that errors if no user is authenticated.
     *
     * @return Mono emitting the username, or error if missing
     */
    Mono<String> requireUsername();

    /**
     * @return a {@link Mono} emitting the full domain {@link User}, fetched from the database.
     */
    Mono<User> currentUser();

    /**
     * Shortcut that errors if no authenticated user can be resolved.
     *
     * @return Mono emitting the {@link User}, or error if missing
     */
    Mono<User> requireUser();

    /**
     * Checks whether the current user has the given role.
     *
     * @param role role to check
     * @return Mono emitting {@code true} if the user has it, else {@code false}
     */
    Mono<Boolean> hasRole(Role role);
}
