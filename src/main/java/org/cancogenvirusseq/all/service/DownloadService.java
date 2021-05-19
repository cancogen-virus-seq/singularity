/*
 * Copyright (c) 2021 The Ontario Institute for Cancer Research. All rights reserved
 *
 * This program and the accompanying materials are made available under the terms of the GNU Affero General Public License v3.0.
 * You should have received a copy of the GNU Affero General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.cancogenvirusseq.all.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.cancogenvirusseq.all.config.elasticsearch.ElasticsearchProperties;
import org.cancogenvirusseq.all.config.elasticsearch.ReactiveElasticSearchClientConfig;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@ConfigurationProperties("download")
public class DownloadService {
  private final ElasticsearchProperties elasticsearchProperties;
  private final ReactiveElasticSearchClientConfig reactiveElasticSearchClientConfig;

  @Setter private Integer isUpdatingWindowMinutes = 10; // default to 10 minutes

  public Mono<Boolean> isIndexBeingUpdated() {
    return Mono.just(
            new SearchSourceBuilder()
                .query(QueryBuilders.matchAllQuery())
                .size(1)
                .sort(new FieldSortBuilder("analysis.updated_at").order(SortOrder.DESC))
                .fetchSource("analysis.updated_at", null))
        .flatMapMany(
            source ->
                reactiveElasticSearchClientConfig
                    .reactiveElasticsearchClient()
                    .search(
                        new SearchRequest()
                            .indices(elasticsearchProperties.getFileCentricIndex())
                            .source(source)))
        .map(hit -> ((Map<String, Long>) hit.getSourceAsMap().get("analysis")).get("updated_at"))
        .reduce(
            false,
            (acc, curr) ->
                acc
                    ? acc
                    : Duration.between(Instant.ofEpochMilli(curr), Instant.now())
                        .minus(Duration.ofMinutes(isUpdatingWindowMinutes))
                        .isNegative());
  }

  public Mono<String> getAllFileIds() {
    return Mono.just(
            new SearchSourceBuilder()
                .query(QueryBuilders.matchAllQuery())
                .sort(new FieldSortBuilder("analysis.updated_at").order(SortOrder.DESC))
                .fetchSource("object_id", null))
        .flatMapMany(
            source ->
                reactiveElasticSearchClientConfig
                    .reactiveElasticsearchClient()
                    .scroll(
                        new SearchRequest()
                            .indices(elasticsearchProperties.getFileCentricIndex())
                            .source(source)))
        .map(hit -> hit.getSourceAsMap().get("object_id").toString())
        .collect(Collectors.joining(","));
  }
}
