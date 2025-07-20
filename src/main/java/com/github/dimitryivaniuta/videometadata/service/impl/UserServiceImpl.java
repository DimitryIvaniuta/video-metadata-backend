package com.github.dimitryivaniuta.videometadata.service.impl;

import com.github.dimitryivaniuta.videometadata.domain.entity.User;
import com.github.dimitryivaniuta.videometadata.repository.UserRepository;
import com.github.dimitryivaniuta.videometadata.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import org.springframework.data.domain.Pageable;

/**
 * Default implementation of {@link UserService}.
 * <p>
 * Uses a blocking JPA {@link UserRepository} beneath a reactive façade.
 * Read operations are {@code readOnly}, and write operations are transactional.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    /**
     * Encoder for hashing user passwords before persistence.
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * Spring Data JPA repository for {@link User} entities.
     */
    private final UserRepository userRepository;

    private static final String USER_NOT_FOUND = "User not found with id: ";

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Mono<UserResponse> createUser(UserCreateRequest request) {
        return Mono.fromCallable(() -> {
                    var user = new User(
                            request.username(),
                            request.email(),
                            passwordEncoder.encode(request.password()),
                            request.roles()
                    );
                    try {
                        var saved = userRepository.save(user);
                        return UserResponse.from(saved);
                    } catch (DataIntegrityViolationException ex) {
                        throw new IllegalArgumentException(
                                "Username or email already exists", ex);
                    }
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Mono<UserResponse> updateUser(Long userId, UserUpdateRequest request) {
        return Mono.fromCallable(() -> {
                    var existing = userRepository.findById(userId)
                            .orElseThrow(() ->
                                    new EntityNotFoundException(USER_NOT_FOUND + userId));

                    if (request.email() != null &&
                            !request.email().equals(existing.getEmail())) {
                        existing.setEmail(request.email());
                    }
                    if (request.password() != null) {
                        existing.setPassword(
                                passwordEncoder.encode(request.password()));
                    }
                    if (request.roles() != null) {
                        existing.setRoleSet(request.roles());
                    }
                    if (request.enabled() != null) {
                        existing.setEnabled(request.enabled());
                    }
                    try {
                        return UserResponse.from(userRepository.save(existing));
                    } catch (DataIntegrityViolationException ex) {
                        throw new IllegalArgumentException(
                                "Email already in use", ex);
                    }
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Mono<Void> deleteUser(Long userId) {
        return Mono.fromRunnable(() -> {
                    if (!userRepository.existsById(userId)) {
                        throw new EntityNotFoundException(USER_NOT_FOUND + userId);
                    }
                    userRepository.deleteById(userId);
                })
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<UserResponse> findById(Long userId) {
        return Mono.fromCallable(() ->
                        userRepository.findById(userId)
                                .orElseThrow(() ->
                                        new EntityNotFoundException(USER_NOT_FOUND + userId)))
                .map(UserResponse::from)
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Flux<UserResponse> findAll(Pageable pageable) {
        return Mono.fromCallable(() ->
                        userRepository.findAll(pageable))
                .flatMapMany(page -> Flux.fromIterable(page.getContent()))
                .map(UserResponse::from)
                .subscribeOn(Schedulers.boundedElastic());
    }
}
