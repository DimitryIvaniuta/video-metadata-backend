package com.github.dimitryivaniuta.videometadata.domain.repository.projection;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;

/**
 * Row in video_import_submission_external_ids.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionExternalId {

    @Id
    private Long id;               // surrogate key for the row (optional)
    private String externalId;     // actual external video id
    private Integer ord;           // to preserve order if needed
}
