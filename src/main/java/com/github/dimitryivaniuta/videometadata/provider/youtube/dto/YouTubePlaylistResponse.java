package com.github.dimitryivaniuta.videometadata.provider.youtube.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Top-level YouTube playlistItems API response.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record YouTubePlaylistResponse(
        @JsonProperty("items") List<PlaylistItem> items,
        @JsonProperty("nextPageToken") String nextPageToken,
        @JsonProperty("pageInfo") PageInfo pageInfo
) {}
