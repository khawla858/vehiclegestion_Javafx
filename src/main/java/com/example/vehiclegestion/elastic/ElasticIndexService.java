
package com.example.vehiclegestion.elastic;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import com.example.vehiclegestion.elastic.ElasticClientConfig;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ElasticIndexService {

    private final ElasticsearchClient client;

    public ElasticIndexService() {
        this.client = ElasticClientConfig.getClient();
    }

    public void sendLog(String level, String message) {
        try {
            Map<String, Object> json = new HashMap<>();
            json.put("level", level);
            json.put("message", message);
            json.put("timestamp", new Date());

            IndexResponse response = client.index(i -> i
                    .index("app-logs")
                    .document(json)
            );

            System.out.println("Indexed log ID: " + response.id());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
