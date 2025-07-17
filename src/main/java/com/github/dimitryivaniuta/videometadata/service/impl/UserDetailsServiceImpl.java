package com.github.dimitryivaniuta.videometadata.service.impl;

import com.github.dimitryivaniuta.videometadata.domain.entity.User;
import com.github.dimitryivaniuta.videometadata.persistence.repository.UserRepository;
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
@Service
public class UserDetailsServiceImpl implements ReactiveUserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(final UserRepository userRepository) {
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
    public Mono<UserDetails> findByUsername(final String username) {
        return Mono.fromCallable(() ->
                        userRepository.findByUsername(username)
                                .orElseThrow(() ->
                                        new UsernameNotFoundException("User not found: " + username))
                )
                // run the blocking call on a scheduler suited for JDBC
                .subscribeOn(Schedulers.boundedElastic())
                // map our domain User -> Spring Security UserDetails
                .map(this::toUserDetails);
    }

    /**
     * Convert our JPA {@link User}
     * into Spring Security's {@link UserDetails}.
     * Uses BCrypt‑encoded password and grants a single ROLE_<ROLE> authority.
     *
     * @param user the JPA entity
     * @return the Spring Security user details
     */
    private UserDetails toUserDetails(final User user) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().name())  // will prefix with "ROLE_"
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}
