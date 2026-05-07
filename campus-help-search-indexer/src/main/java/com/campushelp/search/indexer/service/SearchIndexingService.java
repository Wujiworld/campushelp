package com.campushelp.search.indexer.service;

import com.campushelp.common.event.SearchIndexEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SearchIndexingService {
    private final ElasticsearchOperations elasticsearchOperations;
    private final ObjectMapper objectMapper;

    public SearchIndexingService(ElasticsearchOperations elasticsearchOperations,
                                 ObjectMapper objectMapper) {
        this.elasticsearchOperations = elasticsearchOperations;
        this.objectMapper = objectMapper;
    }

    public void upsert(SearchIndexEvent event) {
        String index = indexName(event.getEntityType());
        IndexQuery query = new IndexQueryBuilder()
                .withId(event.getEntityId())
                .withSource(toJson(event))
                .build();
        elasticsearchOperations.index(query, IndexCoordinates.of(index));
    }

    public void delete(SearchIndexEvent event) {
        String index = indexName(event.getEntityType());
        elasticsearchOperations.delete(event.getEntityId(), IndexCoordinates.of(index));
    }

    private String indexName(String entityType) {
        String type = StringUtils.hasText(entityType) ? entityType.toLowerCase() : "unknown";
        return "campus_" + type;
    }

    private String toJson(SearchIndexEvent event) {
        try {
            return objectMapper.writeValueAsString(event.getPayload());
        } catch (Exception e) {
            throw new IllegalStateException("serialize search payload failed", e);
        }
    }
}
