package com.example.vehiclegestion.logging.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.example.vehiclegestion.logging.model.LogEntry;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service pour envoyer les logs à Elasticsearch de manière asynchrone
 */
public class ElasticLogService {

    private final ElasticsearchClient client;
    private final ExecutorService executor;
    private static final String INDEX_NAME = "auth-logs"; // Index spécifique pour l'authentification

    /**
     * Constructeur - Initialise la connexion Elasticsearch
     */
    public ElasticLogService() {
        RestClient restClient = RestClient.builder(
                new HttpHost("localhost", 9200, "http")
        ).build();

        RestClientTransport transport = new RestClientTransport(
                restClient,
                new JacksonJsonpMapper()
        );

        this.client = new ElasticsearchClient(transport);

        // Thread pool pour les logs asynchrones (évite de bloquer l'application)
        this.executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Envoyer un log à Elasticsearch (version simple - compatibilité)
     */
    public void sendLog(String level, String message) {
        executor.submit(() -> {
            LogEntry log = new LogEntry(level, "GENERIC", "system", message);
            indexLog(log);
        });
    }

    /**
     * Envoyer un LogEntry complet à Elasticsearch
     */
    public void sendLog(LogEntry logEntry) {
        executor.submit(() -> indexLog(logEntry));
    }

    /**
     * Indexer le log dans Elasticsearch
     */
    private void indexLog(LogEntry logEntry) {
        try {
            client.index(i -> i
                    .index(INDEX_NAME)
                    .document(logEntry.toMap())
            );

            System.out.println("📊 Log envoyé à Elasticsearch: " + logEntry.getAction());

        } catch (IOException e) {
            System.err.println("❌ Erreur lors de l'envoi du log à Elasticsearch: " + e.getMessage());
            e.printStackTrace();

            // Fallback: Log dans la console si Elasticsearch est inaccessible
            System.out.println("🔄 Fallback log: " + logEntry);
        }
    }

    /**
     * Vérifier la connexion à Elasticsearch
     */
    public boolean isConnected() {
        try {
            client.ping();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Fermer proprement le service
     */
    public void shutdown() {
        executor.shutdown();
        System.out.println("🛑 ElasticLogService arrêté");
    }
}