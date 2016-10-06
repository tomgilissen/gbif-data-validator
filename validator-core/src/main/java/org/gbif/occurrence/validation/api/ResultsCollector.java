package org.gbif.occurrence.validation.api;

import org.gbif.occurrence.validation.api.model.EvaluationResultDetails;
import org.gbif.occurrence.validation.api.model.EvaluationType;
import org.gbif.occurrence.validation.api.model.RecordEvaluationResult;

import java.util.List;
import java.util.Map;

/**
 * Interface defining the collector of results.
 */
public interface ResultsCollector {

  int DEFAULT_MAX_NUMBER_OF_SAMPLE = 10;

  void accumulate(RecordEvaluationResult result);

  Map<EvaluationType, Long> getAggregatedCounts();

  Map<EvaluationType, List<EvaluationResultDetails>> getSamples();

}
