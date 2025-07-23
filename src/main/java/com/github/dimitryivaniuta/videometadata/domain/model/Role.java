package com.github.dimitryivaniuta.videometadata.domain.model;


/**
 * Application roles.
 * <p>
 * Store them without the {@code ROLE_} prefix internally for readability; expose
 * {@link #asAuthority()} for Spring Security integration.
 */
public enum Role {

    /** Regular authenticated user. */
    USER,

    /** Administrator with elevated privileges. */
    ADMIN;

    /**
     * Returns the Spring Security authority string (\"ROLE_\" + enum name).
     *
     * @return authority representation, e.g. {@code ROLE_ADMIN}.
     */
    public String asAuthority() {
        return "ROLE_" + name();
    }
}