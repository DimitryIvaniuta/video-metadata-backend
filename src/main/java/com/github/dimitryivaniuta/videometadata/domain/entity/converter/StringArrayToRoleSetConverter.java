package com.github.dimitryivaniuta.videometadata.domain.entity.converter;

import com.github.dimitryivaniuta.videometadata.domain.model.Role;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

@ReadingConverter
public class StringArrayToRoleSetConverter implements Converter<String[], Set<Role>> {

    /**
     * Converts the given String array to a role set.
     *
     * @param source array of role names
     * @return set of roles (never empty; defaults to USER if input is null/empty)
     */
    @Override
    public Set<Role> convert(String[] source) {
        if (source.length == 0) {
            return EnumSet.of(Role.USER);
        }
        return Arrays.stream(source)
                .filter(s -> s != null && !s.isBlank())
                .map(String::trim)
                .map(String::toUpperCase)
                .map(s -> s.startsWith("ROLE_") ? s.substring("ROLE_".length()) : s)
                .map(Role::valueOf)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(Role.class)));
    }
}