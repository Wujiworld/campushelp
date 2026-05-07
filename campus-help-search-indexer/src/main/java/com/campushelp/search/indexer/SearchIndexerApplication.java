package com.campushelp.search.indexer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.campushelp")
@EnableScheduling
public class SearchIndexerApplication {
    public static void main(String[] args) {
        SpringApplication.run(SearchIndexerApplication.class, args);
    }
}
