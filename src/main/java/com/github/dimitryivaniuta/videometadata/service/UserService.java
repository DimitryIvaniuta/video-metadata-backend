package com.github.dimitryivaniuta.videometadata.service;

import com.github.dimitryivaniuta.videometadata.domain.dto.UserRequest;
import com.github.dimitryivaniuta.videometadata.domain.dto.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;

/**
 * Business operations on users.
 */
public interface UserService {

    /**
     * Create a new user.
     *
     * @param request user details
     * @return created user
     */
    UserResponse create(UserRequest request);

    /**
     * Delete an existing user.
     *
     * @param id database ID
     */
    void delete(Long id);

    /**
     * Fetch a page of users.
     *
     * @param pageable pagination and sorting
     * @return paged results
     */
    Page<UserResponse> list(Pageable pageable);

    /**
     * Fetch a single user.
     *
     * @param id database ID
     * @return user data
     */
    Mono<UserResponse> findById(Long id);

    /**
     * Update an existing user.
     *
     * @param id      database ID
     * @param request new values
     * @return updated user
     */
    UserResponse update(Long id, UserRequest request);
}
