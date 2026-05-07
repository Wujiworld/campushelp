package com.campushelp.search.indexer.schedule;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SearchReconcileScheduler {
    private final MeterRegistry meterRegistry;

    public SearchReconcileScheduler(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Scheduled(cron = "${campus.search.reconcile-cron:0 */30 * * * *}")
    public void runReconcileSnapshot() {
        meterRegistry.counter("campus.search.reconcile.run").increment();
    }
}
