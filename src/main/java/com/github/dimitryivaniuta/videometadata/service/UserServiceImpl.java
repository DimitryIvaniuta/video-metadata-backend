package com.github.dimitryivaniuta.videometadata.service;

import com.github.dimitryivaniuta.videometadata.domain.dto.UserRequest;
import com.github.dimitryivaniuta.videometadata.domain.dto.UserResponse;
import com.github.dimitryivaniuta.videometadata.domain.entity.User;
import com.github.dimitryivaniuta.videometadata.persistence.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Default implementation of {@link UserService}.
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(
            final UserRepository userRepository,
            final PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserResponse create(final UserRequest request) {
        User user = new User();
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        user.setUsername(request.username());
        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    @Override
    public void delete(final Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(NOT_FOUND, "User not found");
        }
        userRepository.deleteById(id);
    }

    @Override
    public Page<UserResponse> list(final Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::toResponse);
    }

    @Override
    public UserResponse getById(final Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
        return toResponse(user);
    }

    @Override
    public UserResponse update(final Long id, final UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
        // only update mutable fields
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        user.setUsername(request.username());
        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    private UserResponse toResponse(final User u) {
        return new UserResponse(u.getId(), u.getRole(), u.getUsername());
    }
}
