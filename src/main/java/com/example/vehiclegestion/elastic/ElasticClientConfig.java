package com.example.vehiclegestion.elastic;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import org.elasticsearch.client.RestClient;
import org.apache.http.HttpHost;

public class ElasticClientConfig {

    private static ElasticsearchClient client;

    public static ElasticsearchClient getClient() {
        if (client == null) {
            RestClient restClient = RestClient.builder(
                    new HttpHost("localhost", 9200, "http")
            ).build();

            RestClientTransport transport =
                    new RestClientTransport(restClient, new JacksonJsonpMapper());

            client = new ElasticsearchClient(transport);
        }
        return client;
    }
}

