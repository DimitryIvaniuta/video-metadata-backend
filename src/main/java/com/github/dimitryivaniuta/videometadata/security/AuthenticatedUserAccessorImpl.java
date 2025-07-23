package com.github.dimitryivaniuta.videometadata.security;

import com.github.dimitryivaniuta.videometadata.domain.entity.User;
import com.github.dimitryivaniuta.videometadata.domain.model.Role;
import com.github.dimitryivaniuta.videometadata.domain.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Locale;
import java.util.Objects;

/**
 * Default reactive implementation of {@link AuthenticatedUserAccessor}.
 * <p>
 * Supports common {@link Authentication} types:
 * <ul>
 *     <li>{@link UsernamePasswordAuthenticationToken} (form login)</li>
 *     <li>{@link JwtAuthenticationToken} (resource server JWT)</li>
 * </ul>
 * Extend it if you need to handle custom tokens.
 */
@Component
public class AuthenticatedUserAccessorImpl implements AuthenticatedUserAccessor {

    /** Repository for loading {@link User} entities by username. */
    private final UserRepository userRepository;

    /**
     * Creates a new accessor.
     *
     * @param userRepository injected repository
     */
    public AuthenticatedUserAccessorImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** {@inheritDoc} */
    @Override
    public Mono<String> currentUsername() {
        return currentAuthentication()
                .map(this::extractUsername)
                .filter(username -> username != null && !username.isBlank());
    }

    /** {@inheritDoc} */
    @Override
    public Mono<String> requireUsername() {
        return currentUsername()
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("No authenticated principal")));
    }

    /** {@inheritDoc} */
    @Override
    public Mono<User> currentUser() {
        return requireUsername()
                .flatMap(userRepository::findByUsername);
    }

    /** {@inheritDoc} */
    @Override
    public Mono<User> requireUser() {
        return currentUser()
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("Authenticated user not found in database")));
    }

    /** {@inheritDoc} */
    @Override
    public Mono<Boolean> hasRole(Role role) {
        Objects.requireNonNull(role, "role");
        return currentAuthentication()
                .map(Authentication::getAuthorities)
                .defaultIfEmpty(java.util.List.of())
                .map(auths -> containsRole(auths, role));
    }

    /**
     * Retrieves the current {@link Authentication} from the reactive security context.
     *
     * @return Mono emitting the authentication or empty
     */
    private Mono<Authentication> currentAuthentication() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication())
                .filter(Authentication::isAuthenticated);
    }

    /**
     * Extracts the username from the supported authentication types.
     *
     * @param authentication authentication object
     * @return username or {@code null} if not resolvable
     */
    private String extractUsername(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        if (authentication instanceof UsernamePasswordAuthenticationToken) {
            return authentication.getName();
        }
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            // Standard: prefer the "sub" or "preferred_username" claim
            Object preferred = jwtAuth.getToken().getClaims().getOrDefault("preferred_username",
                    jwtAuth.getToken().getSubject());
            return preferred == null ? authentication.getName() : preferred.toString();
        }
        // Fallback to getName()
        return authentication.getName();
    }

    /**
     * Checks whether the authorities collection contains the given role.
     *
     * @param authorities granted authorities
     * @param role        role to check
     * @return true if contains
     */
    private boolean containsRole(Collection<? extends GrantedAuthority> authorities, Role role) {
        String wanted = "ROLE_" + role.name();
        for (GrantedAuthority ga : authorities) {
            if (ga == null) continue;
            String auth = ga.getAuthority();
            if (auth != null && auth.toUpperCase(Locale.ROOT).equals(wanted)) {
                return true;
            }
        }
        return false;
    }
}
