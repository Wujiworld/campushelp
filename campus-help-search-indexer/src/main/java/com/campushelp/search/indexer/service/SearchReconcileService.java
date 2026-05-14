package com.campushelp.search.indexer.service;

import com.campushelp.order.entity.ChActivity;
import com.campushelp.order.entity.ChSecondhandItem;
import com.campushelp.order.mapper.ChActivityMapper;
import com.campushelp.order.mapper.ChSecondhandItemMapper;
import com.campushelp.product.entity.ChProduct;
import com.campushelp.product.entity.ChStore;
import com.campushelp.product.mapper.ChProductMapper;
import com.campushelp.product.mapper.ChStoreMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchReconcileService {

    private static final Logger log = LoggerFactory.getLogger(SearchReconcileService.class);

    private final ElasticsearchOperations esOps;
    private final ChProductMapper productMapper;
    private final ChStoreMapper storeMapper;
    private final ChActivityMapper activityMapper;
    private final ChSecondhandItemMapper secondhandMapper;

    private static final Map<String, EntityDesc> ENTITIES = new LinkedHashMap<>();

    static {
        ENTITIES.put("product", new EntityDesc("campus_product"));
        ENTITIES.put("store", new EntityDesc("campus_store"));
        ENTITIES.put("activity", new EntityDesc("campus_activity"));
        ENTITIES.put("secondhand", new EntityDesc("campus_secondhand"));
    }

    public SearchReconcileService(ElasticsearchOperations esOps,
                                  ChProductMapper productMapper,
                                  ChStoreMapper storeMapper,
                                  ChActivityMapper activityMapper,
                                  ChSecondhandItemMapper secondhandMapper) {
        this.esOps = esOps;
        this.productMapper = productMapper;
        this.storeMapper = storeMapper;
        this.activityMapper = activityMapper;
        this.secondhandMapper = secondhandMapper;
    }

    public ReconcileResult reconcile(String entityType) {
        EntityDesc desc = ENTITIES.get(entityType);
        if (desc == null) {
            log.warn("Unknown reconcile entity type: {}", entityType);
            return new ReconcileResult(entityType);
        }

        Set<String> dbIds = listDbIds(entityType);
        Set<String> esIds = listEsIds(desc.indexName);

        Set<String> missing = new LinkedHashSet<>(dbIds);
        missing.removeAll(esIds);

        Set<String> stale = new LinkedHashSet<>(esIds);
        stale.removeAll(dbIds);

        ReconcileResult r = new ReconcileResult(entityType);
        r.total = dbIds.size();
        r.synced = r.total - missing.size();
        r.missing = missing;
        r.stale = stale;

        if (!missing.isEmpty()) {
            log.warn("Reconcile [{}] missing in ES: {} records", entityType, missing.size());
        }
        if (!stale.isEmpty()) {
            log.info("Reconcile [{}] stale in ES (deleted from DB): {} records", entityType, stale.size());
        }
        log.info("Reconcile [{}] done: total={}, synced={}, missing={}, stale={}",
                entityType, r.total, r.synced, missing.size(), stale.size());

        return r;
    }

    public Map<String, ReconcileResult> reconcileAll() {
        Map<String, ReconcileResult> results = new LinkedHashMap<>();
        for (String type : ENTITIES.keySet()) {
            results.put(type, reconcile(type));
        }
        return results;
    }

    public int reindexMissing(String entityType) {
        // TODO: implement when business code starts publishing SearchIndexEvent
        log.warn("Reindex not yet supported for [{}] — needs SearchIndexEvent publishing in business layer", entityType);
        return 0;
    }

    private Set<String> listDbIds(String entityType) {
        switch (entityType) {
            case "product":
                return ids(productMapper.selectList(null), ChProduct::getId);
            case "store":
                return ids(storeMapper.selectList(null), ChStore::getId);
            case "activity":
                return ids(activityMapper.selectList(null), ChActivity::getId);
            case "secondhand":
                return ids(secondhandMapper.selectList(null), ChSecondhandItem::getId);
            default:
                return Collections.emptySet();
        }
    }

    private Set<String> listEsIds(String indexName) {
        var results = esOps.search(
                new CriteriaQuery(Criteria.where("_id").exists()),
                Object.class,
                IndexCoordinates.of(indexName));
        return results.getSearchHits().stream()
                .map(SearchHit::getId)
                .collect(Collectors.toSet());
    }

    private static <T> Set<String> ids(List<T> list, java.util.function.Function<T, Long> idFn) {
        return list.stream().map(idFn).map(String::valueOf).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static class EntityDesc {
        final String indexName;
        EntityDesc(String indexName) { this.indexName = indexName; }
    }

    public static class ReconcileResult {
        public final String entityType;
        public int total;
        public int synced;
        public Set<String> missing = Collections.emptySet();
        public Set<String> stale = Collections.emptySet();

        ReconcileResult(String entityType) { this.entityType = entityType; }
    }
}
