package com.github.dimitryivaniuta.videometadata.dto.provider.youtube;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * YouTube playlist/listItems response (simplified).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record YouTubePlaylistResponse(
        @JsonProperty("kind") String kind,
        @JsonProperty("etag") String etag,
        @JsonProperty("items") List<PlaylistItem> items,
        @JsonProperty("next_page_token") String nextPageToken,
        @JsonProperty("prev_page_token") String prevPageToken,
        @JsonProperty("page_info") PageInfo pageInfo
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PageInfo(
            @JsonProperty("total_results") int totalResults,
            @JsonProperty("results_per_page") int resultsPerPage
    ) {
    }
}