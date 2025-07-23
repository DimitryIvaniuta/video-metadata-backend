package com.github.dimitryivaniuta.videometadata.web.dto.user;

import java.time.Instant;

public record SetLastLoginRequest(Instant moment) {
}
