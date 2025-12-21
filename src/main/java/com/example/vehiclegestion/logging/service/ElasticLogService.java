package com.example.vehiclegestion.logging.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.example.vehiclegestion.logging.model.LogEntry;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ElasticLogService {

    private final ElasticsearchClient client;
    private final ExecutorService executor;
    private static final String INDEX_NAME = "app-logs";

    public ElasticLogService() {
        RestClient restClient = RestClient.builder(
                new HttpHost("localhost", 9200, "http")
        ).build();

        ElasticsearchTransport transport = new RestClientTransport(
                restClient,
                new JacksonJsonpMapper()
        );

        this.client = new ElasticsearchClient(transport);
        this.executor = Executors.newSingleThreadExecutor();
    }

    // Méthode 1 : version vendeur
    public void sendLog(String level, String message, Map<String, Object> details) {
        executor.submit(() -> {
            Map<String, Object> log = new HashMap<>();
            log.put("level", level);
            log.put("message", message);
            log.put("timestamp", new Date());

            if (details != null) log.putAll(details);

            try {
                client.index(i -> i.index(INDEX_NAME).document(log));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    // Méthode 2 : version simple (auth/admin)
    public void sendLog(String level, String message) {
        sendLog(level, message, null); // appelle la version précédente
    }

    // Méthode 3 : version LogEntry (auth/admin)
    public void sendLog(LogEntry logEntry) {
        Map<String,Object> map = logEntry.toMap();
        sendLog((String)map.get("level"), (String)map.get("message"), map);
    }
}
