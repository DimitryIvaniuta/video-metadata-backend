package com.github.dimitryivaniuta.videometadata.dto.provider.vimeo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Vimeo listing response.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record VimeoVideoResponse(
        @JsonProperty("total") Integer total,
        @JsonProperty("page") Integer page,
        @JsonProperty("per_page") Integer perPage,
        @JsonProperty("data") List<VimeoVideoItem> data,
        @JsonProperty("paging") Paging paging
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Paging(
            @JsonProperty("next") String next,
            @JsonProperty("previous") String previous,
            @JsonProperty("first") String first,
            @JsonProperty("last") String last
    ) {}
}