package com.github.dimitryivaniuta.videometadata.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Domain entity representing a platform user (local authentication principal).
 * <p>
 * Features:
 * <ul>
 *     <li>Sequence-based primary key shared across entities (VM_UNIQUE_ID).</li>
 *     <li>Unique username (required) & optional unique email.</li>
 *     <li>Roles persisted as a comma-separated list of uppercase tokens (e.g. {@code "ROLE_ADMIN,ROLE_USER"}).</li>
 *     <li>BCrypt hashed password only (never store raw).</li>
 *     <li>Auditing fields populated by Spring Data JPA.</li>
 *     <li>Lightweight role management helper methods.</li>
 * </ul>
 * <b>Security Note:</b> Password must be encoded (BCrypt) before persisting;
 * service layer / listener should enforce this contract.
 */
@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_username", columnNames = "username"),
                @UniqueConstraint(name = "uk_users_email", columnNames = "email")
        },
        indexes = {
                @Index(name = "idx_users_username", columnList = "username"),
                @Index(name = "idx_users_email", columnList = "email")
        }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@ToString(exclude = {"videos"})
public class User {

    /**
     * Surrogate primary key from global sequence {@code VM_UNIQUE_ID}.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "VM_UNIQUE_ID")
    @SequenceGenerator(
            name = "VM_UNIQUE_ID",
            sequenceName = "VM_UNIQUE_ID",
            allocationSize = 10
    )
    private Long id;

    /**
     * Account enabled flag. If false, authentication should be denied.
     */
    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    /**
     * Optional contact email (may also be used for password recovery).
     */
    @Email
    @Size(max = 255)
    @Column(name = "email", length = 255)
    private String email;

    /**
     * Account locked flag (e.g. after security events). If true, logins should be blocked.
     */
    @Column(name = "locked", nullable = false)
    private boolean locked = false;

    /**
     * BCrypt hashed password. Must never contain raw secret.
     */
    @NotBlank
    @Size(max = 200)
    @Column(name = "password", nullable = false, length = 200)
    private String password;

    /**
     * Comma-separated uppercase role tokens (e.g. ROLE_ADMIN,ROLE_USER).
     */
    @NotBlank
    @Size(max = 500)
    @Column(name = "roles", nullable = false, length = 500)
    private String roles;

    /**
     * Username (unique, case-sensitive unless service layer normalizes).
     */
    @NotBlank
    @Size(max = 150)
    @Column(name = "username", nullable = false, length = 150)
    private String username;

    /* ======================================================================
       Auditing
       ====================================================================== */

    /**
     * Creation timestamp.
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Last update timestamp.
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /* ======================================================================
       Relationships
       ====================================================================== */

    /**
     * Videos imported / created by this user (inverse side).
     */
    @OneToMany(mappedBy = "createdBy", fetch = FetchType.LAZY)
    private Set<Video> videos = new HashSet<>();

    /* ======================================================================
       Constructors
       ====================================================================== */

    protected User() {
        // JPA
    }

    /**
     * Convenience constructor for service layer.
     */
    public User(String username, String email, String password, Collection<String> roleSet) {
        this.username = username;
        this.email = email;
        this.password = password;
        setRoleSet(roleSet == null ? Set.of("ROLE_USER") : new HashSet<>(roleSet));
        this.enabled = true;
        this.locked = false;
    }

    /* ======================================================================
       Roles Handling
       ====================================================================== */

    /**
     * Returns immutable set of role tokens.
     *
     * @return Set of roles (never null).
     */
    public Set<String> getRoleSet() {
        if (roles == null || roles.isBlank()) {
            return Collections.emptySet();
        }
        return Arrays.stream(roles.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Overwrites roles from a provided collection, normalizing each token to uppercase and ensuring prefix "ROLE_".
     *
     * @param newRoles new roles to apply.
     */
    public void setRoleSet(Collection<String> newRoles) {
        if (newRoles == null || newRoles.isEmpty()) {
            this.roles = "ROLE_USER";
            return;
        }
        String joined = newRoles.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(r -> r.toUpperCase(Locale.ROOT))
                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                .distinct()
                .sorted()
                .collect(Collectors.joining(","));
        this.roles = joined.isEmpty() ? "ROLE_USER" : joined;
    }

    /**
     * Adds a single role.
     *
     * @param role role to add.
     */
    public void addRole(String role) {
        Set<String> rs = new HashSet<>(getRoleSet());
        rs.add(role);
        setRoleSet(rs);
    }

    /**
     * Removes a role if present.
     *
     * @param role role to remove.
     */
    public void removeRole(String role) {
        Set<String> rs = new HashSet<>(getRoleSet());
        rs.remove(role);
        setRoleSet(rs);
    }

    /**
     * @return Collection of {@link SimpleGrantedAuthority} for security adapters.
     */
    public Collection<SimpleGrantedAuthority> toGrantedAuthorities() {
        return getRoleSet().stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    /* ======================================================================
       Status Helpers
       ====================================================================== */

    /**
     * Indicates whether account is eligible for authentication.
     *
     * @return true if enabled and not locked.
     */
    public boolean isActive() {
        return enabled && !locked;
    }

    /* ======================================================================
       Equality / HashCode
       ====================================================================== */

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> effectiveClass = (this instanceof HibernateProxy proxy)
                ? proxy.getHibernateLazyInitializer().getPersistentClass()
                : getClass();
        Class<?> otherEffectiveClass = (o instanceof HibernateProxy proxy)
                ? proxy.getHibernateLazyInitializer().getPersistentClass()
                : o.getClass();
        if (effectiveClass != otherEffectiveClass) return false;
        User other = (User) o;
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return (this instanceof HibernateProxy proxy)
                ? proxy.getHibernateLazyInitializer().getPersistentClass().hashCode()
                : getClass().hashCode();
    }

    /* ======================================================================
       Lifecycle / Utility
       ====================================================================== */

    /**
     * Ensures minimal role baseline after entity load if data drift occurs.
     */
    @PostLoad
    private void enforceRoleInvariant() {
        if (roles == null || roles.isBlank()) {
            roles = "ROLE_USER";
        }
    }
}
