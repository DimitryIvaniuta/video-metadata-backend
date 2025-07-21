package com.github.dimitryivaniuta.videometadata.domain.repository;

import com.github.dimitryivaniuta.videometadata.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for {@link User} entities.
 * <p>
 * Provides CRUD plus lookup by username.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by their unique username.
     *
     * @param username the username to search
     * @return an {@link Optional} containing the {@link User} if found
     */
    Optional<User> findByUsername(String username);

    /**
     * List all users paginated.
     * <p>
     * Inherited from {@link JpaRepository}, but declared here
     * for clarity in service layer use.
     *
     * @param pageable pagination parameters
     * @return a page of users
     */
    Page<User> findAll(Pageable pageable);
}
