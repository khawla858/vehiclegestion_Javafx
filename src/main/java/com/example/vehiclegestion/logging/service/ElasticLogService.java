package com.example.vehiclegestion.logging.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.example.vehiclegestion.logging.model.LogEntry;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

import java.io.IOException;
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
        this.executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Envoie un objet LogEntry complet à Elasticsearch
     */
    public void sendLog(LogEntry logEntry) {
        executor.submit(() -> {
            try {
                client.index(i -> i
                        .index("app-logs")
                        .document(logEntry)
                );
                System.out.println("✅ Log envoyé à Elasticsearch: " + logEntry.getAction());
            } catch (IOException e) {
                System.err.println("❌ Erreur envoi log Elasticsearch: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Méthode alternative pour envoyer un log simple (backward compatibility)
     */
    public void sendLog(String level, String message) {
        LogEntry logEntry = new LogEntry();
        logEntry.setLevel(level);
        logEntry.setMessage(message);
        logEntry.setAction("LOG_MESSAGE");
        sendLog(logEntry);
    }

    public void shutdown() {
        executor.shutdown();
    }
}