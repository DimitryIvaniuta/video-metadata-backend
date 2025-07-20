package com.github.dimitryivaniuta.videometadata.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Reactive accessor for current authenticated user attributes.
 */
@Component
public class AuthenticatedUserAccessor {

    /**
     * Returns current username or empty if none.
     */
    public Mono<String> currentUsername() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication())
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getName);
    }

    public Mono<Long> currentUserId(UserLookupPort lookupPort) {
        return currentUsername()
                .flatMap(lookupPort::findUserIdByUsername); // returns Mono<Long>
    }

}