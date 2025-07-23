package com.github.dimitryivaniuta.videometadata.domain.entity.converter;

import org.springframework.core.convert.converter.Converter;
import com.github.dimitryivaniuta.videometadata.domain.model.Role;
import org.springframework.data.convert.WritingConverter;

import java.util.EnumSet;
import java.util.Set;

/**
 * Converts a {@link Set<Role>} to a Postgres text[] (as {@link String}[]).
 */
@WritingConverter
public class RoleSetToStringArrayConverter implements Converter<Set<Role>, String[]> {

    /**
     * Converts the given role set to an array of uppercase role strings with the {@code ROLE_} prefix.
     *
     * @param source set of roles
     * @return array of role strings suitable for a text[] column
     */
    @Override
    public String[] convert(Set<Role> source) {
        Set<Role> safe = source.isEmpty() ? EnumSet.of(Role.USER) : source;
        return safe.stream()
                .map(Role::asAuthority)
                .toArray(String[]::new);
    }
}