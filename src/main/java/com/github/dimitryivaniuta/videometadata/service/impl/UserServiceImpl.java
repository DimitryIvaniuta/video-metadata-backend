package com.github.dimitryivaniuta.videometadata.service.impl;

import com.github.dimitryivaniuta.videometadata.domain.entity.User;
import com.github.dimitryivaniuta.videometadata.domain.model.Role;
import com.github.dimitryivaniuta.videometadata.domain.repository.UserRepository;
import com.github.dimitryivaniuta.videometadata.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;

/**
 * Default reactive implementation of {@link UserService}.
 * Uses {@link TransactionalOperator} for write operations to ensure atomicity with R2DBC.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    /** Password encoder (e.g., BCryptPasswordEncoder). */
    private final PasswordEncoder passwordEncoder;

    /** Reactive transaction operator. */
    private final TransactionalOperator tx;

    /** Repository for user persistence. */
    private final UserRepository userRepository;

    @Override
    public Mono<User> createUser(String username, String email, String rawPassword, Set<Role> roles) {
        Objects.requireNonNull(username, "username");
        Objects.requireNonNull(rawPassword, "rawPassword");

        return ensureUnique(username, email)
                .then(Mono.defer(() -> {
                    User u = new User();
                    u.setUsername(username);
                    u.setEmail(email);
                    u.setPassword(passwordEncoder.encode(rawPassword));
                    u.setRoleSet(roles == null || roles.isEmpty() ? Set.of(Role.USER) : roles);
                    u.setEnabled(true);
                    u.setLocked(false);
                    return userRepository.save(u);
                }))
                .as(tx::transactional)
                .onErrorMap(DuplicateKeyException.class,
                        ex -> new IllegalStateException("Username or email already exists", ex));
    }

    @Override
    public Mono<User> getById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Mono<User> getByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Mono<User> updateProfile(Long id, String username, String email) {
        Objects.requireNonNull(id, "id");

        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("User not found: " + id)))
                .flatMap(existing -> ensureUniqueForUpdate(existing, username, email)
                        .then(Mono.defer(() -> {
                            if (username != null && !username.isBlank()) {
                                existing.setUsername(username);
                            }
                            if (email != null && !email.isBlank()) {
                                existing.setEmail(email);
                            }
                            return userRepository.save(existing);
                        })))
                .as(tx::transactional);
    }

    @Override
    public Mono<User> replaceRoles(Long id, Set<Role> roles) {
        Objects.requireNonNull(id, "id");

        Set<Role> safe = (roles == null || roles.isEmpty()) ? Set.of(Role.USER) : roles;

        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("User not found: " + id)))
                .flatMap(u -> userRepository.replaceRoles(u.getId(), safe)
                        .flatMap(updated -> updated ? userRepository.findById(id)
                                : Mono.error(new IllegalStateException("Roles not updated"))))
                .as(tx::transactional);
    }

    @Override
    public Mono<Void> changePassword(Long id, String rawPassword) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(rawPassword, "rawPassword");

        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("User not found: " + id)))
                .flatMap(u -> {
                    u.setPassword(passwordEncoder.encode(rawPassword));
                    return userRepository.save(u).then();
                })
                .as(tx::transactional);
    }

    @Override
    public Mono<Void> setEnabled(Long id, boolean enabled) {
        Objects.requireNonNull(id, "id");
        return userRepository.updateEnabled(id, enabled)
                .flatMap(ok -> ok ? Mono.<Void>empty()
                        : Mono.error(new IllegalArgumentException("User not found: " + id)))
                .as(tx::transactional);
    }

    @Override
    public Mono<Void> setLocked(Long id, boolean locked) {
        Objects.requireNonNull(id, "id");
        return userRepository.updateLocked(id, locked)
                .flatMap(ok -> ok ? Mono.<Void>empty()
                        : Mono.error(new IllegalArgumentException("User not found: " + id)))
                .as(tx::transactional);
    }

    @Override
    public Mono<Void> updateLastLogin(Long id, Instant moment) {
        Objects.requireNonNull(id, "id");
        Instant ts = moment == null ? Instant.now() : moment;
        return userRepository.updateLastLoginAt(id, ts).then();
    }

    @Override
    public Mono<Void> delete(Long id) {
        Objects.requireNonNull(id, "id");
        return userRepository.deleteById(id).as(tx::transactional);
    }

    @Override
    public Flux<User> searchByUsername(String fragment, int offset, int limit) {
        return userRepository.searchByUsername(fragment, offset, limit);
    }

    /* ---------- helpers ---------- */

    private Mono<Void> ensureUnique(String username, String email) {
        Mono<Boolean> usernameExists = userRepository.existsByUsername(username);
        Mono<Boolean> emailExists = (email == null || email.isBlank())
                ? Mono.just(false)
                : userRepository.existsByEmail(email);

        return Mono.zip(usernameExists, emailExists)
                .flatMap(t -> {
                    if (t.getT1()) return Mono.error(new IllegalStateException("Username already in use: " + username));
                    if (t.getT2()) return Mono.error(new IllegalStateException("Email already in use: " + email));
                    return Mono.empty();
                });
    }

    private Mono<Void> ensureUniqueForUpdate(User current, String username, String email) {
        Mono<Void> usernameCheck = Mono.empty();
        Mono<Void> emailCheck = Mono.empty();

        if (username != null && !username.equals(current.getUsername())) {
            usernameCheck = userRepository.existsByUsername(username)
                    .flatMap(exists -> exists
                            ? Mono.error(new IllegalStateException("Username already in use: " + username))
                            : Mono.empty());
        }

        if (email != null && !email.equals(current.getEmail()) && !email.isBlank()) {
            emailCheck = userRepository.existsByEmail(email)
                    .flatMap(exists -> exists
                            ? Mono.error(new IllegalStateException("Email already in use: " + email))
                            : Mono.empty());
        }

        return Mono.when(usernameCheck, emailCheck).then();
    }
}
