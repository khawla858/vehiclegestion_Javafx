package com.example.vehiclegestion.logging.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
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

    public ElasticLogService() {

        RestClient restClient = RestClient.builder(
                        new HttpHost("localhost", 9200, "http"))
                .build();

        ElasticsearchTransport transport = new RestClientTransport(
                restClient,
                new JacksonJsonpMapper()
        );

        this.client = new ElasticsearchClient(transport);

        // Créer un thread pour envoyer les logs en arrière-plan
        this.executor = Executors.newSingleThreadExecutor();
    }

    public void sendLog(String level, String message, Map<String, Object> details) {
        executor.submit(() -> {
            Map<String, Object> log = new HashMap<>();
            log.put("level", level);
            log.put("message", message);
            log.put("timestamp", new Date());

            // Ajouter les détails si fournis
            if (details != null) {
                log.putAll(details);
            }

            try {
                client.index(i -> i
                        .index("app-logs")
                        .document(log)
                );
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

}

