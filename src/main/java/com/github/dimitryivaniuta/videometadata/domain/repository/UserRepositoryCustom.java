package com.github.dimitryivaniuta.videometadata.domain.repository;

import com.github.dimitryivaniuta.videometadata.domain.entity.User;
import com.github.dimitryivaniuta.videometadata.domain.model.Role;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Set;

/**
 * Custom reactive data-access contract for {@link User} that extends what
 * {@link org.springframework.data.repository.reactive.ReactiveCrudRepository} offers.
 * <p>
 * Implement this interface in a class named <b>UserRepositoryCustomImpl</b> (Spring Data
 * picks it up automatically) or register it manually as a bean.
 */
public interface UserRepositoryCustom {

    /**
     * Updates the {@code last_login_at} column for the given user.
     *
     * @param userId user identifier
     * @param moment timestamp to set (UTC instant recommended)
     * @return {@link Mono} emitting {@code true} if at least one row was updated; otherwise {@code false}
     */
    Mono<Boolean> updateLastLoginAt(Long userId, Instant moment);

    /**
     * Toggles the {@code enabled} flag for the given user.
     *
     * @param userId  user identifier
     * @param enabled new enabled value
     * @return {@link Mono} emitting {@code true} if at least one row was updated; otherwise {@code false}
     */
    Mono<Boolean> updateEnabled(Long userId, boolean enabled);

    /**
     * Toggles the {@code locked} flag for the given user.
     *
     * @param userId user identifier
     * @param locked new locked value
     * @return {@link Mono} emitting {@code true} if at least one row was updated; otherwise {@code false}
     */
    Mono<Boolean> updateLocked(Long userId, boolean locked);

    /**
     * Replaces the complete set of roles for the given user.
     *
     * @param userId user identifier
     * @param roles  new roles to persist; if {@code null} or empty, the implementation should normalize to {@link Role#USER}
     * @return {@link Mono} emitting {@code true} if at least one row was updated; otherwise {@code false}
     */
    Mono<Boolean> replaceRoles(Long userId, Set<Role> roles);

    /**
     * Case-insensitive substring search on usernames.
     * <p>
     * Provide explicit offset/limit because Spring Data R2DBC has no Pageable abstraction yet.
     *
     * @param fragment substring to search within usernames (case-insensitive); empty/null means match all
     * @param offset   zero-based starting row offset
     * @param limit    maximum number of rows to return (must be &gt; 0)
     * @return {@link Flux} emitting users that match the search criteria
     */
    Flux<User> searchByUsername(String fragment, int offset, int limit);
}
