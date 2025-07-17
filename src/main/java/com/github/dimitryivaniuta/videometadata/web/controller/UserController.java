package com.github.dimitryivaniuta.videometadata.web.controller;

import com.github.dimitryivaniuta.videometadata.domain.dto.UserRequest;
import com.github.dimitryivaniuta.videometadata.domain.dto.UserResponse;
import com.github.dimitryivaniuta.videometadata.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Exposes RESTful CRUD operations on users.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(final UserService userService) {
        this.userService = userService;
    }

    /**
     * List users (admin only).
     *
     * @param page zero-based page index
     * @param size page size
     * @param sort comma-separated sort properties (e.g. \"username,desc\")
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<UserResponse> list(
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "20") final int size,
            @RequestParam(defaultValue = "id,asc") final String sort
    ) {
        String[] parts = sort.split(",");
        Sort s = Sort.by(Sort.Direction.fromString(parts[1]), parts[0]);
        Pageable pg = PageRequest.of(page, size, s);
        return userService.list(pg);
    }

    /**
     * Get a single user (admin or user).
     *
     * @param id user ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public UserResponse getById(@PathVariable final Long id) {
        return userService.getById(id);
    }

    /**
     * Create a new user (admin only).
     *
     * @param request payload
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse create(@RequestBody final UserRequest request) {
        return userService.create(request);
    }

    /**
     * Update an existing user (admin only).
     *
     * @param id      user ID
     * @param request payload
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse update(
            @PathVariable final Long id,
            @RequestBody final UserRequest request
    ) {
        return userService.update(id, request);
    }

    /**
     * Delete a user (admin only).
     *
     * @param id user ID
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable final Long id) {
        userService.delete(id);
    }
}
