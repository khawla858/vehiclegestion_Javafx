package com.example.vehiclegestion.logging.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.WildcardQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.example.vehiclegestion.logging.model.LogEntry;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

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


import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ElasticLogService {

    private final ElasticsearchClient client;
    private final ExecutorService executor;

    public ElasticLogService() {
        RestClient restClient = RestClient.builder(
                new HttpHost("localhost", 9200, "http")
        ).build();

        RestClientTransport transport = new RestClientTransport(
                restClient,
                new JacksonJsonpMapper()
        );

        this.client = new ElasticsearchClient(transport);
        this.executor = Executors.newSingleThreadExecutor();
    }

    // ===== ENVOI DE LOGS =====
    public void sendLog(LogEntry logEntry) {
        executor.submit(() -> {
            try {
                String index = resolveIndex(logEntry.getModule());

                client.index(i -> i
                        .index(index)
                        .document(logEntry.toMap())
                );

                System.out.println("📊 Log envoyé → " + index + " | " + logEntry.getAction());

            } catch (IOException e) {
                System.err.println("❌ Elasticsearch indisponible: " + e.getMessage());
            }
        });
    }

    // ===== RECHERCHE DE LOGS =====

    /**
     * Récupérer tous les logs (limité aux 1000 derniers)
     */
    public List<Map<String, Object>> getAllLogs() {
        return searchLogs(null, null, null, null, 1000);
    }

    /**
     * Recherche avancée de logs avec filtres
     *
     * @param level Niveau de log (ERROR, WARN, INFO, etc.) - null pour tous
     * @param action Action spécifique - null pour toutes
     * @param module Module (AUTH, CLIENT, SYSTEM) - null pour tous
     * @param searchText Recherche texte libre - null pour tout
     * @param maxResults Nombre maximum de résultats
     * @return Liste des logs trouvés
     */
    public List<Map<String, Object>> searchLogs(String level, String action,
                                                String module, String searchText,
                                                int maxResults) {
        List<Map<String, Object>> logs = new ArrayList<>();

        try {
            // Déterminer les indices à rechercher
            String[] indices = determineIndices(module);

            // Construire la requête bool
            BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

            // Filtre par niveau
            if (level != null && !level.isEmpty() && !"Tous".equalsIgnoreCase(level)) {
                boolQueryBuilder.must(Query.of(q -> q.term(TermQuery.of(t -> t
                        .field("level.keyword")
                        .value(level)))));
            }

            // Filtre par action
            if (action != null && !action.isEmpty() && !"Toutes".equalsIgnoreCase(action)) {
                boolQueryBuilder.must(Query.of(q -> q.wildcard(WildcardQuery.of(w -> w
                        .field("action.keyword")
                        .value("*" + action + "*")))));
            }

            // Recherche texte libre
            if (searchText != null && !searchText.isEmpty()) {
                boolQueryBuilder.must(Query.of(q -> q.multiMatch(MultiMatchQuery.of(mm -> mm
                        .query(searchText)
                        .fields("message", "userEmail", "action", "level")))));
            }

            // Si aucun filtre, match_all
            Query finalQuery;
            if (boolQueryBuilder.build().must().isEmpty() &&
                    boolQueryBuilder.build().should().isEmpty() &&
                    boolQueryBuilder.build().filter().isEmpty()) {
                finalQuery = Query.of(q -> q.matchAll(ma -> ma));
            } else {
                finalQuery = Query.of(q -> q.bool(boolQueryBuilder.build()));
            }

            // Créer la requête de recherche avec tri corrigé
            SearchResponse<Map> response = client.search(s -> s
                            .index(Arrays.asList(indices))
                            .size(maxResults)
                            .query(finalQuery)
                            .sort(sort -> sort
                                    .field(f -> f
                                            .field("timestamp")
                                            .order(SortOrder.Desc))),
                    Map.class
            );

            // Extraire les résultats
            for (Hit<Map> hit : response.hits().hits()) {
                Map<String, Object> source = hit.source();
                if (source != null) {
                    logs.add(source);
                }
            }

            System.out.println("📊 Logs récupérés: " + logs.size() + " résultats");

        } catch (IOException e) {
            System.err.println("❌ Erreur lors de la recherche de logs: " + e.getMessage());
            e.printStackTrace();
        }

        return logs;
    }

    /**
     * Récupérer les logs par plage de dates
     */
    public List<Map<String, Object>> getLogsByDateRange(LocalDateTime startDate,
                                                        LocalDateTime endDate,
                                                        int maxResults) {
        List<Map<String, Object>> logs = new ArrayList<>();

        try {
            Query rangeQuery = Query.of(q -> q
                    .range(r -> r
                            .date(d -> d
                                    .field("timestamp")
                                    .gte(startDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                                    .lte(endDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                            )
                    )
            );


            SearchResponse<Map> response = client.search(s -> s
                            .index("auth-logs", "client-logs", "system-logs")
                            .size(maxResults)
                            .query(rangeQuery)
                            .sort(sort -> sort
                                    .field(f -> f
                                            .field("timestamp")
                                            .order(SortOrder.Desc))),
                    Map.class
            );

            for (Hit<Map> hit : response.hits().hits()) {
                if (hit.source() != null) {
                    logs.add(hit.source());
                }
            }

        } catch (IOException e) {
            System.err.println("❌ Erreur recherche par date: " + e.getMessage());
            e.printStackTrace();
        }

        return logs;
    }

    /**
     * Compter les logs par niveau
     */
    public Map<String, Long> countLogsByLevel() {
        Map<String, Long> counts = new HashMap<>();
        counts.put("ERROR", 0L);
        counts.put("WARN", 0L);
        counts.put("INFO", 0L);
        counts.put("SUCCESS", 0L);

        try {
            SearchResponse<Map> response = client.search(s -> s
                            .index("auth-logs", "client-logs", "system-logs")
                            .size(0)
                            .query(Query.of(q -> q.matchAll(ma -> ma)))
                            .aggregations("by_level", a -> a
                                    .terms(t -> t.field("level.keyword"))),
                    Map.class
            );

            // Traiter les agrégations
            if (response.aggregations() != null) {
                var levelAgg = response.aggregations().get("by_level");
                if (levelAgg != null && levelAgg.isSterms()) {
                    levelAgg.sterms().buckets().array().forEach(bucket -> {
                        counts.put(bucket.key().stringValue(), bucket.docCount());
                    });
                }
            }

        } catch (IOException e) {
            System.err.println("❌ Erreur comptage logs: " + e.getMessage());
        }

        return counts;
    }

    /**
     * Supprimer tous les logs
     */
    public void clearAllLogs() {
        try {
            client.deleteByQuery(d -> d
                    .index("auth-logs", "client-logs", "system-logs")
                    .query(Query.of(q -> q.matchAll(ma -> ma)))
            );

            System.out.println("🗑️ Tous les logs ont été supprimés");

        } catch (IOException e) {
            System.err.println("❌ Erreur suppression logs: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ===== MÉTHODES PRIVÉES =====

    private String resolveIndex(String module) {
        return switch (module) {
            case LogEntry.MODULE_AUTH -> "auth-logs";
            case LogEntry.MODULE_CLIENT -> "client-logs";
            default -> "system-logs";
        };
    }

    private String[] determineIndices(String module) {
        if (module == null || module.isEmpty()) {
            return new String[]{"auth-logs", "client-logs", "system-logs"};
        }
        return new String[]{resolveIndex(module)};
    }
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