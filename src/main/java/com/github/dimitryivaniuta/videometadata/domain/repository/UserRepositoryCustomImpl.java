package com.github.dimitryivaniuta.videometadata.domain.repository;

import com.github.dimitryivaniuta.videometadata.domain.entity.User;
import com.github.dimitryivaniuta.videometadata.domain.model.Role;
import org.springframework.dao.DataAccessException;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * Reactive PostgreSQL implementation of {@link UserRepositoryCustom} using {@link DatabaseClient}.
 * <p>
 * All methods are non-blocking and safe to use on the Reactor event loop. SQL is tuned for Postgres.
 */
@Repository
public class UserRepositoryCustomImpl implements UserRepositoryCustom {

    /** Reactive database client used to execute parameterized SQL statements. */
    private final DatabaseClient client;

    /**
     * Creates a new instance.
     *
     * @param client injected {@link DatabaseClient}
     */
    public UserRepositoryCustomImpl(DatabaseClient client) {
        this.client = client;
    }

    /** {@inheritDoc} */
    @Override
    public Mono<Boolean> updateLastLoginAt(Long userId, Instant moment) {
        final String sql = """
                UPDATE users
                   SET last_login_at = :moment
                 WHERE id = :id
                """;
        return rowsUpdated(sql, "id", userId, "moment", moment);
    }

    /** {@inheritDoc} */
    @Override
    public Mono<Boolean> updateEnabled(Long userId, boolean enabled) {
        final String sql = """
                UPDATE users
                   SET enabled = :enabled
                 WHERE id = :id
                """;
        return rowsUpdated(sql, "id", userId, "enabled", enabled);
    }

    /** {@inheritDoc} */
    @Override
    public Mono<Boolean> updateLocked(Long userId, boolean locked) {
        final String sql = """
                UPDATE users
                   SET locked = :locked
                 WHERE id = :id
                """;
        return rowsUpdated(sql, "id", userId, "locked", locked);
    }

    /** {@inheritDoc} */
    @Override
    public Mono<Boolean> replaceRoles(Long userId, Set<Role> roles) {
        String[] roleArray = (roles == null || roles.isEmpty())
                ? new String[]{Role.USER.asAuthority()}
                : roles.stream().map(Role::asAuthority).toArray(String[]::new);

        final String sql = "UPDATE users SET roles = CAST(:roles AS text[]) WHERE id = :id";

        return client.sql(sql)
                .bind("roles", roleArray)
                .bind("id", userId)
                .fetch()
                .rowsUpdated()
                .map(count -> count != null && count > 0)
                .onErrorResume(DataAccessException.class, Mono::error);
    }

    /** {@inheritDoc} */
    @Override
    public Flux<User> searchByUsername(String fragment, int offset, int limit) {
        final String sql = """
                SELECT id,
                       username,
                       email,
                       password,
                       enabled,
                       locked,
                       roles,
                       created_at,
                       updated_at,
                       last_login_at
                  FROM users
                 WHERE LOWER(username) LIKE LOWER(CONCAT('%', :frag, '%'))
                 ORDER BY username ASC
                 OFFSET :offset LIMIT :limit
                """;

        return client.sql(sql)
                .bind("frag", fragment == null ? "" : fragment.trim())
                .bind("offset", Math.max(0, offset))
                .bind("limit", Math.max(1, limit))
                .map((row, meta) -> mapRowToUser(row))
                .all();
    }

    /**
     * Helper that executes an UPDATE and returns a boolean indicating whether any row was affected.
     *
     * @param sql     SQL statement with two named parameters
     * @param name1   first parameter name
     * @param value1  first parameter value
     * @param name2   second parameter name
     * @param value2  second parameter value
     * @return {@link Mono} emitting {@code true} if at least one row updated
     */
    private Mono<Boolean> rowsUpdated(String sql, String name1, Object value1, String name2, Object value2) {
        return client.sql(sql)
                .bind(name1, value1)
                .bind(name2, value2)
                .fetch()
                .rowsUpdated()
                .map(count -> count != null && count > 0)
                .onErrorResume(DataAccessException.class, Mono::error);
    }

    /**
     * Maps a database row to a {@link User} entity.
     * <p>
     * Relies on registered Spring Data converters for {@code roles}. If the driver returns a {@code String[]},
     * we convert it manually.
     *
     * @param row R2DBC row
     * @return populated {@link User}
     */
    private User mapRowToUser(io.r2dbc.spi.Row row) {
        User u = new User();
        u.setId(row.get("id", Long.class));
        u.setUsername(row.get("username", String.class));
        u.setEmail(row.get("email", String.class));
        u.setPassword(row.get("password", String.class));
        u.setEnabled(Boolean.TRUE.equals(row.get("enabled", Boolean.class)));
        u.setLocked(Boolean.TRUE.equals(row.get("locked", Boolean.class)));
        u.setCreatedAt(row.get("created_at", Instant.class));
        u.setUpdatedAt(row.get("updated_at", Instant.class));
        u.setLastLoginAt(row.get("last_login_at", Instant.class));

        Object rolesObj = row.get("roles");
        if (rolesObj instanceof Set) {
            @SuppressWarnings("unchecked")
            Set<Role> set = (Set<Role>) rolesObj;
            u.setRoleSet(set);
        } else if (rolesObj instanceof String[] arr) {
            Set<Role> set = new HashSet<>();
            for (String s : arr) {
                if (s == null || s.isBlank()) continue;
                String normalized = s.trim().toUpperCase();
                if (normalized.startsWith("ROLE_")) {
                    normalized = normalized.substring("ROLE_".length());
                }
                set.add(Role.valueOf(normalized));
            }
            if (set.isEmpty()) {
                set.add(Role.USER);
            }
            u.setRoleSet(set);
        } else {
            // Fallback if column is null or unexpected
            u.setRoleSet(Set.of(Role.USER));
        }

        return u;
    }
}
