package com.campushelp.search.indexer.schedule;

import com.campushelp.search.indexer.service.SearchReconcileService;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SearchReconcileScheduler {

    private static final Logger log = LoggerFactory.getLogger(SearchReconcileScheduler.class);

    private final MeterRegistry meterRegistry;
    private final SearchReconcileService reconcileService;

    public SearchReconcileScheduler(MeterRegistry meterRegistry,
                                     SearchReconcileService reconcileService) {
        this.meterRegistry = meterRegistry;
        this.reconcileService = reconcileService;
    }

    @Scheduled(cron = "${campus.search.reconcile-cron:0 */30 * * * *}")
    public void runReconcileSnapshot() {
        meterRegistry.counter("campus.search.reconcile.run").increment();
        try {
            Map<String, SearchReconcileService.ReconcileResult> results = reconcileService.reconcileAll();
            for (var entry : results.entrySet()) {
                var r = entry.getValue();
                meterRegistry.gauge("campus.search.reconcile.total",
                        Map.of("entityType", entry.getKey()), r.total);
                meterRegistry.gauge("campus.search.reconcile.synced",
                        Map.of("entityType", entry.getKey()), r.synced);
                meterRegistry.gauge("campus.search.reconcile.missing",
                        Map.of("entityType", entry.getKey()), r.missing.size());
                meterRegistry.gauge("campus.search.reconcile.stale",
                        Map.of("entityType", entry.getKey()), r.stale.size());
            }
        } catch (Exception e) {
            log.error("Search reconcile failed", e);
            meterRegistry.counter("campus.search.reconcile.error").increment();
        }
    }
}
