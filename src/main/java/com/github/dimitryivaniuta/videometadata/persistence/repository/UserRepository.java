package com.github.dimitryivaniuta.videometadata.persistence.repository;

import java.util.List;
import java.util.Optional;

import com.github.dimitryivaniuta.videometadata.domain.entity.Role;
import com.github.dimitryivaniuta.videometadata.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * CRUD and custom queries for User entities.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Lookup a user by username.
     *
     * @param username the login name
     * @return empty if none found
     */
    Optional<User> findByUsername(String username);

    /**
     * Find all users having the given role.
     *
     * @param role the role to filter
     * @return list of matching users
     */
    List<User> findAllByRole(Role role);
}