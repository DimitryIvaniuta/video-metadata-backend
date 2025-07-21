package com.github.dimitryivaniuta.videometadata.web.dto.provider.vimeo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * DTO representing a paged response from Vimeo’s /videos endpoint.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record VimeoVideoResponse(
        // Video title as returned by Vimeo.
        @JsonProperty("name")
        String name,

        // Duration in seconds.
        @JsonProperty("duration")
        long duration,

        // Creation timestamp in ISO‑8601 format.
        @JsonProperty("created_time")
        ZonedDateTime createdTime,

        // Total number of items available.
        @JsonProperty("total")
        Integer total,

        // Current page number.
        @JsonProperty("page")
        Integer page,

        // Number of items per page.
        @JsonProperty("per_page")
        Integer perPage,

        // List of individual video items in this page.
        @JsonProperty("data")
        List<VimeoVideoItem> data,

        // Pagination links.
        @JsonProperty("paging")
        Paging paging
) {

    /**
     * Inner DTO for Vimeo pagination links.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Paging(
            // URL of the next page, if any.
            @JsonProperty("next")
            String next,

            // URL of the previous page, if any.
            @JsonProperty("previous")
            String previous,

            // URL of the first page.
            @JsonProperty("first")
            String first,

            // URL of the last page.
            @JsonProperty("last")
            String last
    ) {}
}
