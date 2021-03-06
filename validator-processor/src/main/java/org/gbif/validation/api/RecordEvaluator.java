package org.gbif.validation.api;

import org.gbif.validation.api.model.RecordEvaluationResult;

import java.util.List;
import javax.annotation.Nullable;

/**
 * {@link RecordEvaluator} is responsible to take a record and produce an {@link RecordEvaluationResult}.
 */
public interface RecordEvaluator {

  /**
   * Evaluate a record represented as an array of values (as String).
   *
   * @param lineNumber number of the line within the context, can be null
   * @param record     values
   *
   * @return the result of the evaluation or null if no result can be generated (e.g. empty record)
   */
  @Nullable
  RecordEvaluationResult evaluate(@Nullable Long lineNumber, @Nullable List<String> record);

}
