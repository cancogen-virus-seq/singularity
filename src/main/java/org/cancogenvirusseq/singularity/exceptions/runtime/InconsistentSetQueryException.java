package org.cancogenvirusseq.singularity.exceptions.runtime;

import static java.lang.String.format;

public class InconsistentSetQueryException extends RuntimeException {
  public InconsistentSetQueryException(Long expected, Long actual) {
    super(
        format(
            "Set Document expected %d documents, but %d were found when executing the terms lookup query",
            expected, actual));
  }
}
