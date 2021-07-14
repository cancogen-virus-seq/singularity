package org.cancogenvirusseq.singularity.repository.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.UUID;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Table("archive_set_query")
public class ArchiveSetQuery {
  @Id private UUID id;
  @NonNull private ArchiveStatus status;
  @NonNull private Long timestamp;
  private UUID objectId;
  private ArchiveMeta meta;
  @NonNull private String setQueryHash;

  @RequiredArgsConstructor
  public enum Fields {
    meta("meta"),
    setQueryHash("set_query_hash"),
    timestamp("timestamp"),
    status("status"),
    type("type"),
    objectId("object_id"),
    id("id");

    private final String text;

    public String toString() {
      return text;
    }
  }
}
