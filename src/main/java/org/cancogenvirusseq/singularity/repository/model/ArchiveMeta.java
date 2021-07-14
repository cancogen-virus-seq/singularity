package org.cancogenvirusseq.singularity.repository.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArchiveMeta {
  Integer numOfDownloads;
  Integer numOfSamples;

  @RequiredArgsConstructor
  public enum Fields {
    numOfDownloads("num_of_downloads"),
    numOfSamples("num_of_samples");

    private final String text;

    public String toString() {
      return text;
    }
  }
}
