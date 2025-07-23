package com.github.dimitryivaniuta.videometadata.domain.entity;

import com.github.dimitryivaniuta.videometadata.domain.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Reactive Spring Data (R2DBC) entity representing an application user.
 * <p>
 * Key points:
 * <ul>
 *     <li>No JPA/Hibernate annotations or lazy relationships. Fetch related aggregates reactively via repositories.</li>
 *     <li>Roles are stored as a comma-separated list of uppercased tokens with the {@code ROLE_} prefix.</li>
 *     <li>Passwords must already be encoded (e.g., BCrypt) before persisting.</li>
 *     <li>Auditing fields ({@link #createdAt}, {@link #updatedAt}) are populated by Spring Data R2DBC auditing.</li>
 * </ul>
 * <b>Security note:</b> Never store a raw password in {@link #password}.
 */
@Table("users")
@Getter
@Setter
@ToString
public class User {

    /** Primary key in the {@code users} table. */
    @Id
    @Column("id")
    private Long id;

    /** Timestamp when the user row was created (set automatically by Spring Data auditing). */
    @CreatedDate
    @Column("created_at")
    private Instant createdAt;

    /** Optional but unique email address of the user. */
    @Email
    @Size(max = 254)
    @Column("email")
    private String email;

    /** Flag indicating whether the account is enabled. Disabled users cannot authenticate. */
    @Column("enabled")
    private boolean enabled = true;

    /** Timestamp of the last successful login, if tracked. */
    @Column("last_login_at")
    private Instant lastLoginAt;

    /** If true, the account is locked and authentication attempts should be rejected. */
    @Column("locked")
    private boolean locked = false;

    /** BCrypt-hashed password (never raw). */
    @NotBlank
    @Size(max = 200)
    @Column("password")
    private String password;

    /**
     * Set of roles granted to the user.
     * <p>
     * Persisted as a PostgreSQL {@code text[]} column using custom converters.
     * Defaults to containing {@link Role#USER}.
     */
    @Column("roles")
    private Set<Role> roles = Set.of(Role.USER);

    /** Timestamp when the user row was last updated (set automatically by Spring Data auditing). */
    @LastModifiedDate
    @Column("updated_at")
    private Instant updatedAt;

    /** Unique username (case sensitivity depends on service-layer normalization). */
    @NotBlank
    @Size(max = 150)
    @Column("username")
    private String username;

    /**
     * Returns the roles as a mutable {@link Set}.
     *
     * @return a new mutable set of roles; never empty.
     */
    public Set<Role> getRoleSet() {
        if (roles == null || roles.isEmpty()) {
            return new HashSet<>(Set.of(Role.USER));
        }
        return new HashSet<>(roles);
    }

    /**
     * Maps the roles to Spring Security {@link SimpleGrantedAuthority} objects.
     *
     * @return set of authorities corresponding to the stored roles.
     */
    public Set<SimpleGrantedAuthority> getAuthoritySet() {
        return getRoleSet()
                .stream()
                .map(Role::asAuthority)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }

    /**
     * Overwrites the stored roles with the provided collection.
     * <p>
     * If {@code newRoles} is null or empty, it falls back to {@link Role#USER}.
     *
     * @param newRoles new roles to store.
     */
    public void setRoleSet(Collection<Role> newRoles) {
        if (newRoles == null || newRoles.isEmpty()) {
            this.roles = Set.of(Role.USER);
            return;
        }
        this.roles = newRoles.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());
        enforceRoleInvariant();
    }

    /**
     * Adds a role to the current role set.
     *
     * @param role role to add.
     */
    public void addRole(Role role) {
        Set<Role> rs = getRoleSet();
        rs.add(role);
        setRoleSet(rs);
    }

    /**
     * Removes a role from the current role set. If set becomes empty,
     * {@link Role#USER} is re-added to maintain the baseline invariant.
     *
     * @param role role to remove.
     */
    public void removeRole(Role role) {
        Set<Role> rs = getRoleSet();
        rs.remove(role);
        setRoleSet(rs);
    }

    /**
     * Ensures {@link #roles} is never null/empty; if so, it restores {@link Role#USER}.
     */
    private void enforceRoleInvariant() {
        if (this.roles == null || this.roles.isEmpty()) {
            this.roles = Set.of(Role.USER);
        }
    }

    /**
     * Equality is based on the primary key {@link #id}. If either side has a null id, falls back to reference equality.
     *
     * @param o other object
     * @return true if both represent the same persisted row
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return id != null && id.equals(user.id);
    }

    /**
     * Hash code based on {@link #id}; returns 0 if id is null.
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        return id == null ? 0 : id.hashCode();
    }
}
