package org.cancogenvirusseq.singularity.components.pipelines;

import static org.cancogenvirusseq.singularity.components.utils.PostgresUtils.getSqlStateOptionalFromException;
import static org.cancogenvirusseq.singularity.components.utils.PostgresUtils.isUniqueViolationError;

import java.util.UUID;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cancogenvirusseq.singularity.components.base.CountAndLastUpdatedAggregation;
import org.cancogenvirusseq.singularity.components.base.GetArrangerSetDocument;
import org.cancogenvirusseq.singularity.components.events.ArchiveBuildRequestEmitter;
import org.cancogenvirusseq.singularity.components.model.ArchiveBuildRequest;
import org.cancogenvirusseq.singularity.components.model.ArrangerSetDocument;
import org.cancogenvirusseq.singularity.components.model.CountAndLastUpdatedResult;
import org.cancogenvirusseq.singularity.components.model.SetQueryArchiveHashInfo;
import org.cancogenvirusseq.singularity.config.elasticsearch.ElasticsearchProperties;
import org.cancogenvirusseq.singularity.exceptions.runtime.InconsistentSetQueryException;
import org.cancogenvirusseq.singularity.repository.ArchivesRepo;
import org.cancogenvirusseq.singularity.repository.model.Archive;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.indices.TermsLookup;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class SetQueryArchiveRequest implements Function<UUID, Mono<Archive>> {
  private final ArchiveBuildRequestEmitter archiveBuildRequestEmitter;

  private final ElasticsearchProperties elasticsearchProperties;
  private final ArchivesRepo archivesRepo;

  private final GetArrangerSetDocument getArrangerSetDocument;
  private final CountAndLastUpdatedAggregation countAndLastUpdatedAggregation;

  // aggregation name constants
  private static final String TERMS_LOOKUP_FIELD = "_id";
  private static final String TERMS_LOOKUP_PATH = "ids";

  @Override
  public Mono<Archive> apply(UUID setId) {
    return getArrangerSetDocument
        .apply(setId)
        .flatMap(arrangerSetDocumentToSetQueryHashInfoFunctionForSetId(setId))
        .map(Archive::newFromSetQueryArchiveHashInfo)
        .flatMap(saveAndTriggerBuildOrGetArchiveFunctionForSetId(setId));
  }

  private Function<ArrangerSetDocument, Mono<SetQueryArchiveHashInfo>>
      arrangerSetDocumentToSetQueryHashInfoFunctionForSetId(UUID setId) {
    return arrangerSetDocument ->
        Mono.just(arrangerSetTermsQuery(setId))
            .flatMap(countAndLastUpdatedAggregation)
            .map(checkSetQueryVsAggregation(arrangerSetDocument))
            .map(
                countAndLastUpdatedResult ->
                    new SetQueryArchiveHashInfo(
                        arrangerSetDocument.getSqon(),
                        arrangerSetDocument.getSize(),
                        countAndLastUpdatedResult.getLastUpdatedDate().getValueAsString()))
            .onErrorStop();
  }

  private QueryBuilder arrangerSetTermsQuery(UUID setId) {
    return QueryBuilders.termsLookupQuery(
        TERMS_LOOKUP_FIELD,
        new TermsLookup(
            elasticsearchProperties.getArrangerSetsIndex(), setId.toString(), TERMS_LOOKUP_PATH));
  }

  private UnaryOperator<CountAndLastUpdatedResult> checkSetQueryVsAggregation(
      ArrangerSetDocument arrangerSetDocument) {
    return countAndLastUpdatedResult -> {
      if (!arrangerSetDocument
          .getSize()
          .equals(countAndLastUpdatedResult.getNumDocuments().getValue())) {
        throw new InconsistentSetQueryException(
            arrangerSetDocument.getSize(), countAndLastUpdatedResult.getNumDocuments().getValue());
      }

      return countAndLastUpdatedResult;
    };
  }

  private Function<Archive, Mono<Archive>> saveAndTriggerBuildOrGetArchiveFunctionForSetId(
      UUID setId) {
    return archive ->
        archivesRepo
            .save(archive)
            // why this? because R2DBC does not hydrate fields
            // (https://github.com/spring-projects/spring-data-r2dbc/issues/455)
            .flatMap(archivesRepo::findByArchiveObject)
            // this onSuccess will only execute when the archive is created and will not be
            // triggered by
            // the onErrorResume
            .doOnSuccess(
                createdArchive ->
                    archiveBuildRequestEmitter
                        .getSink()
                        .tryEmitNext(
                            new ArchiveBuildRequest(createdArchive, arrangerSetTermsQuery(setId))))
            // in the event of a duplicate insert, return the existing archive
            .onErrorResume(
                DataIntegrityViolationException.class,
                dataViolation ->
                    getSqlStateOptionalFromException(dataViolation)
                        .filter(isUniqueViolationError)
                        .map(
                            uniqueConstraint ->
                                archivesRepo.findArchiveByHashInfoEquals(archive.getHashInfo()))
                        .orElseThrow(() -> dataViolation));
  }
}
