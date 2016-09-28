package org.gbif.occurrence.validation.api;

import org.gbif.api.vocabulary.EvaluationDetailType;
import org.gbif.api.vocabulary.EvaluationType;
import org.gbif.occurrence.validation.model.EvaluationResult;
import org.gbif.occurrence.validation.model.EvaluationResultDetails;

import java.util.List;
import java.util.Map;

/**
 * Interface defining the collector of results.
 * @param <R>
 */
public interface ResultsCollector<R> {

  void accumulate(EvaluationResult result);

  Map<EvaluationType, Map<EvaluationDetailType, Long>> getAggregatedCounts();

  Map<EvaluationType, Map<EvaluationDetailType, List<EvaluationResultDetails>>> getSamples();

}
