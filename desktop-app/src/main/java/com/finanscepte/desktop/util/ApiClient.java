package com.finanscepte.desktop.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ApiClient {

    public static final String BASE_URL = "http://localhost:8080";
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static String authToken;

    public static String currentUserId;

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

    public static String put(String path, String jsonBody) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path));
        if (jsonBody != null && !jsonBody.isBlank()) {
            builder.header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonBody));
        } else {
            builder.PUT(HttpRequest.BodyPublishers.noBody());
        }
        if (authToken != null) builder.header("Authorization", "Bearer " + authToken);
        HttpResponse<String> r = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        checkError(r);
        return r.body();
    }

    public static void put(String path) throws Exception {
        put(path, null);
    }

    public static void patch(String path) throws Exception {
        patch(path, null);
    }

    public static void patch(String path, String jsonBody) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path));
        if (jsonBody != null && !jsonBody.isBlank()) {
            builder.header("Content-Type", "application/json")
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonBody));
        } else {
            builder.method("PATCH", HttpRequest.BodyPublishers.noBody());
        }
        if (authToken != null) builder.header("Authorization", "Bearer " + authToken);
        HttpResponse<String> r = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        checkError(r);
    }

    public static byte[] getBytes(String path) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path)).GET();
        if (authToken != null) builder.header("Authorization", "Bearer " + authToken);
        HttpResponse<byte[]> r = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofByteArray());
        checkErrorBytes(r);
        return r.body();
    }

    private static void checkErrorBytes(HttpResponse<byte[]> r) throws Exception {
        int code = r.statusCode();
        if (code >= 400) {
            throw new RuntimeException("HTTP " + code);
        }
    }

    private static void checkError(HttpResponse<String> r) throws Exception {
        int code = r.statusCode();
        if (code >= 400) {
            String body = r.body();
            if (body == null || body.isBlank()) {
                throw new RuntimeException("HTTP " + code + ": Sunucu hatası (boş cevap)");
            }
            try {
                var err = objectMapper.readTree(body);
                String msg = null;
                if (err.has("message")) msg = err.get("message").asText();
                else if (err.has("error")) msg = err.get("error").asText();
                if (msg == null || msg.isBlank()) msg = body;
                throw new RuntimeException("HTTP " + code + ": " + msg);
            } catch (RuntimeException re) { throw re; }
              catch (Exception e) {
                throw new RuntimeException("HTTP " + code + ": " + body);
            }
        }
    }
}
