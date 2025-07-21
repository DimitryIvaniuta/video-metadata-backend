package com.github.dimitryivaniuta.videometadata.service.impl;

import com.github.dimitryivaniuta.videometadata.domain.entity.User;
import com.github.dimitryivaniuta.videometadata.domain.model.Role;
import com.github.dimitryivaniuta.videometadata.web.dto.user.UserCreateRequest;
import com.github.dimitryivaniuta.videometadata.domain.repository.UserRepository;
import com.github.dimitryivaniuta.videometadata.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
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

/**
 * Default implementation of both the synchronous and reactive {@link UserService}.
 * <p>
 * Synchronous methods invoke Spring Data JPA directly.<br>
 * Reactive methods wrap the synchronous calls on a boundedElastic scheduler.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    /**
     * JPA repository for User entities.
     */
    private final UserRepository userRepository;

    /**
     * Encoder for hashing user passwords.
     */
    private final PasswordEncoder passwordEncoder;

    // ---------- Synchronous (blocking) API ----------

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public User createUser(UserCreateRequest request) {
        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        user.setEnabled(true);
        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException("Username or email already exists", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<User> listUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public User updateUser(Long id,
                           String newPassword,
                           Role newRole,
                           Boolean enabled) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        if (newPassword != null) {
            existing.setPassword(passwordEncoder.encode(newPassword));
        }
        if (newRole != null) {
            existing.setRole(newRole);
        }
        if (enabled != null) {
            existing.setEnabled(enabled);
        }
        return userRepository.save(existing);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            // idempotent: nothing to do if absent
            return;
        }
        userRepository.deleteById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    // ---------- Reactive convenience wrappers ----------

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<User> createUserMono(UserCreateRequest request) {
        return Mono.fromCallable(() -> createUser(request))
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<User> findByIdMono(Long id) {
        return Mono.fromCallable(() ->
                        findById(id)
                                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id)))
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<User> findByUsernameMono(String username) {
        return Mono.fromCallable(() ->
                        findByUsername(username)
                                .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + username)))
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Flux<User> listUsersFlux(Pageable pageable) {
        return Mono.fromCallable(() -> listUsers(pageable))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(page -> Flux.fromIterable(page.getContent()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<User> updateUserMono(Long id,
                                     String newPassword,
                                     Role newRole,
                                     Boolean enabled) {
        return Mono.fromCallable(() -> updateUser(id, newPassword, newRole, enabled))
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Void> deleteUserMono(Long id) {
        return Mono.fromRunnable(() -> deleteUser(id))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }
}
