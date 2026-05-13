package Genex.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Properties;

public class GroqService {
    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private final HttpClient httpClient;
    private final String apiKey;

    public GroqService() {
        this.httpClient = HttpClient.newHttpClient();
        this.apiKey = resolveApiKey();
    }

    public String generateForumDescription(String forumTitle) {
        if (isBlank(apiKey)) {
            throw new IllegalStateException("Configuration Groq manquante. Définissez GROQ_API_KEY.");
        }

        try {
            String requestBody = buildRequestBody(forumTitle);
            System.out.println("[GROQ] ========== REQUÊTE GROQ ==========");
            System.out.println("[GROQ] URL: " + API_URL);
            System.out.println("[GROQ] Titre demandé: " + forumTitle);
            System.out.println("[GROQ] ====================================");
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            System.out.println("[GROQ] Envoi requête...");
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("[GROQ] ========== RÉPONSE GROQ ==========");
            System.out.println("[GROQ] Code réponse: " + response.statusCode());
            System.out.println("[GROQ] Body complet: " + response.body());
            System.out.println("[GROQ] ====================================");

            if (response.statusCode() == 200) {
                String result = extractTextFromResponse(response.body());
                System.out.println("[GROQ] ✓ Résultat final: " + result);
                return result;
            } else {
                System.err.println("[GROQ] ✗ ERREUR HTTP " + response.statusCode());
                String apiMessage = extractErrorMessage(response.body());
                System.err.println("[GROQ] Message: " + apiMessage);
                if (response.statusCode() == 401) {
                    throw new IllegalStateException("Clé API Groq invalide. Vérifiez GROQ_API_KEY.");
                }
                throw new IllegalStateException("Erreur API Groq (code " + response.statusCode() + "): " + apiMessage);
            }
        } catch (IOException e) {
            System.err.println("[GROQ] IOException: " + e.getMessage());
            throw new IllegalStateException("Connexion à Groq impossible: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            System.err.println("[GROQ] InterruptedException: " + e.getMessage());
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Requête Groq interrompue.", e);
        }
    }

    private String buildRequestBody(String forumTitle) {
        JsonObject request = new JsonObject();
        request.addProperty("model", "llama-3.1-8b-instant");
        request.addProperty("max_tokens", 150);

        JsonArray messages = new JsonArray();
        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", "Génère une description courte et attrayante pour un forum intitulé '" + forumTitle + "'. "
                + "La description doit être en français, d'une ou deux phrases maximum, et encourager les utilisateurs à participer.");

        messages.add(message);
        request.add("messages", messages);

        return request.toString();
    }

    private String extractTextFromResponse(String responseBody) {
        try {
            JsonObject response = JsonParser.parseString(responseBody).getAsJsonObject();
            JsonArray choices = response.getAsJsonArray("choices");
            if (choices != null && choices.size() > 0) {
                JsonObject choice = choices.get(0).getAsJsonObject();
                JsonObject message = choice.getAsJsonObject("message");
                if (message != null && message.has("content")) {
                    return message.get("content").getAsString().trim();
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur parsing réponse Groq: " + e.getMessage());
        }
        throw new IllegalStateException("Réponse Groq invalide.");
    }

    private String extractErrorMessage(String responseBody) {
        try {
            JsonObject response = JsonParser.parseString(responseBody).getAsJsonObject();
            JsonObject error = response.getAsJsonObject("error");
            if (error != null && error.has("message")) {
                return error.get("message").getAsString();
            }
        } catch (JsonSyntaxException | IllegalStateException ignored) {
            // fallback below
        }
        return responseBody == null || responseBody.isBlank() ? "Erreur inconnue." : responseBody;
    }

    private String resolveApiKey() {
        Properties localConfig = loadLocalConfig();
        // Prefer the local properties file first for local development (non-versioned)
        String fromLocal = localConfig.getProperty("groq.api.key");
        if (!isBlank(fromLocal)) {
            return fromLocal.trim();
        }
        // Fall back to environment variable or system property
        return resolveValue("GROQ_API_KEY", "groq.api.key", localConfig);
    }

    private Properties loadLocalConfig() {
        Properties properties = new Properties();
        try (InputStream input = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("groq.local.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Impossible de charger groq.local.properties: " + e.getMessage(), e);
        }
        return properties;
    }

    private String resolveValue(String envKey, String propertyKey, Properties localConfig) {
        String fromEnv = System.getenv(envKey);
        if (!isBlank(fromEnv)) {
            return fromEnv.trim();
        }
        String fromSystemProperty = System.getProperty(propertyKey);
        if (!isBlank(fromSystemProperty)) {
            return fromSystemProperty.trim();
        }
        String fromLocal = localConfig.getProperty(propertyKey);
        if (!isBlank(fromLocal)) {
            return fromLocal.trim();
        }
        return "";
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
