package com.github.dimitryivaniuta.videometadata.service;

import com.github.dimitryivaniuta.videometadata.domain.entity.User;
import com.github.dimitryivaniuta.videometadata.domain.model.Role;
import com.github.dimitryivaniuta.videometadata.exception.ResourceNotFoundException;
import com.github.dimitryivaniuta.videometadata.persistence.repository.UserRepository;
import com.github.dimitryivaniuta.videometadata.web.dto.UserCreateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Default implementation of {@link UserService}.
 * <p>
 * Because JPA operations are blocking, reactive wrappers offload work
 * to {@code boundedElastic} so as not to block Netty event loops.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    // ---- Alphabetically sorted private fields (as requested) ----
    private final PasswordEncoder passwordEncoder;
    private final UserRepository   userRepository;

    // =============================================================
    // Synchronous (blocking) API
    // =============================================================

    @Override
    @Transactional
    public User createUser(final UserCreateRequest request) {
        requireNonNull(request, "request must not be null");
        validateCreateRequest(request);

        if (userRepository.existsByUsername(request.username())) {
            throw new DataIntegrityViolationException(
                    "Username already exists: " + request.username()
            );
        }

        final User user = User.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .role(request.role())
                .enabled(true)
                .build();

        final User saved = userRepository.save(user);
        log.info("Created user id={} username={} role={}", saved.getId(), saved.getUsername(), saved.getRole());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(final Long id) {
        return userRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(final String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> listUsers(final Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public User updateUser(final Long id,
                           final String newPassword,
                           final Role newRole,
                           final Boolean enabled) {

        final User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found id=" + id));

        boolean modified = false;

        if (newPassword != null && !newPassword.isBlank()) {
            user.setPassword(passwordEncoder.encode(newPassword));
            modified = true;
        }

        if (newRole != null && newRole != user.getRole()) {
            user.setRole(newRole);
            modified = true;
        }

        if (enabled != null && enabled != user.isEnabled()) {
            user.setEnabled(enabled);
            modified = true;
        }

        if (!modified) {
            log.debug("No changes applied to user id={}", id);
            return user;
        }

        final User saved = userRepository.save(user);
        log.info("Updated user id={} role={} enabled={}", saved.getId(), saved.getRole(), saved.isEnabled());
        return saved;
    }

    @Override
    @Transactional
    public void deleteUser(final Long id) {
        if (!userRepository.existsById(id)) {
            // idempotent delete; can also throw if you prefer strictness
            log.debug("Delete requested for non-existing user id={}", id);
            return;
        }
        userRepository.deleteById(id);
        log.info("Deleted user id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    // =============================================================
    // Reactive wrappers
    // =============================================================

    @Override
    public Mono<User> createUserMono(final UserCreateRequest request) {
        return Mono.fromCallable(() -> createUser(request))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<User> findByIdMono(final Long id) {
        return Mono.fromCallable(() -> findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found id=" + id)))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<User> findByUsernameMono(final String username) {
        return Mono.fromCallable(() -> findByUsername(username)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found username=" + username)))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Flux<User> listUsersFlux(final Pageable pageable) {
        return Mono.fromCallable(() -> listUsers(pageable))
                .flatMapMany(page -> Flux.fromIterable(page.getContent()))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<User> updateUserMono(final Long id,
                                     final String newPassword,
                                     final Role newRole,
                                     final Boolean enabled) {
        return Mono.fromCallable(() -> updateUser(id, newPassword, newRole, enabled))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Void> deleteUserMono(final Long id) {
        return Mono.fromRunnable(() -> deleteUser(id))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    // =============================================================
    // Internal validation helpers
    // =============================================================

    private void validateCreateRequest(UserCreateRequest request) {
        if (request.username() == null || request.username().isBlank()) {
            throw new IllegalArgumentException("username must not be blank");
        }
        if (request.password() == null || request.password().isBlank()) {
            throw new IllegalArgumentException("password must not be blank");
        }
        if (request.role() == null) {
            throw new IllegalArgumentException("role must not be null");
        }
    }
}
