package com.github.dimitryivaniuta.videometadata.security;

import com.github.dimitryivaniuta.videometadata.domain.entity.User;
import com.github.dimitryivaniuta.videometadata.domain.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.security.core.userdetails.User as SpringUser;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Reactive user‑details service that loads users from the database and
 * adapts them into Spring Security {@link UserDetails}.
 * <p>
 * Wraps the blocking JPA repository calls on a bounded‑elastic scheduler
 * so as not to block the WebFlux event‑loop.
 * </p>
 */
@Slf4j
@Service
public class ReactiveUserDetailsServiceImpl implements ReactiveUserDetailsService {

    private final UserRepository userRepository;

    public ReactiveUserDetailsServiceImpl(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Looks up a user by username and converts it into a {@link UserDetails}
     * instance, or errors with {@link UsernameNotFoundException} if not found.
     *
     * @param username the login name to look up
     * @return a {@link Mono} emitting the {@link UserDetails}
     */
    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("User '" + username + "' not found")))
                .map(this::mapToUserDetails);
    }

    /**
     * Convert our JPA {@link User}
     * into Spring Security's {@link UserDetails}.
     * Uses BCrypt‑encoded password and grants a single ROLE_<ROLE> authority.
     *
     * @param user the JPA entity
     * @return the Spring Security user details
     */
    private UserDetails mapToUserDetails(final User user) {
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())                     // already encoded
                .authorities(user.getAuthoritySet())               // ROLE_* authorities
                .accountExpired(false)                             // adjust if you track expiry
                .accountLocked(user.isLocked())
                .credentialsExpired(false)                         // adjust if you track password expiry
                .disabled(!user.isEnabled())
                .build();
    }
}
