package com.finanscepte.desktop.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ApiClient {

    private static final String BASE_URL = "http://localhost:8080";
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static String authToken;

    public static void setAuthToken(String token) { authToken = token; }

    public static <T> T get(String path, Class<T> responseType) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path)).GET();
        if (authToken != null) builder.header("Authorization", "Bearer " + authToken);
        HttpResponse<String> r = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        checkError(r);
        return objectMapper.readValue(r.body(), responseType);
    }

    public static void delete(String path) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path)).DELETE();
        if (authToken != null) builder.header("Authorization", "Bearer " + authToken);
        HttpResponse<String> r = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        checkError(r);
    }

    public static String post(String path, String jsonBody) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody));
        if (authToken != null) builder.header("Authorization", "Bearer " + authToken);
        HttpResponse<String> r = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        checkError(r);
        return r.body();
    }

    public static void patch(String path) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .method("PATCH", HttpRequest.BodyPublishers.noBody());
        if (authToken != null) builder.header("Authorization", "Bearer " + authToken);
        HttpResponse<String> r = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        checkError(r);
    }

    private static void checkError(HttpResponse<String> r) throws Exception {
        int code = r.statusCode();
        if (code >= 400) {
            String body = r.body();
            try {
                var err = objectMapper.readTree(body);
                String msg = err.has("message") ? err.get("message").asText() : body;
                throw new RuntimeException("HTTP " + code + ": " + msg);
            } catch (RuntimeException re) { throw re; }
              catch (Exception e) {
                throw new RuntimeException("HTTP " + code + ": " + body);
            }
        }
    }
}
