package Genex.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class GroqService {
    private static final String API_KEY = System.getenv("GROQ_API_KEY") != null 
        ? System.getenv("GROQ_API_KEY") 
        : "your-api-key-here";
    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private final HttpClient httpClient;

    public GroqService() {
        this.httpClient = HttpClient.newHttpClient();
    }

    public String generateForumDescription(String forumTitle) {
        try {
            String requestBody = buildRequestBody(forumTitle);
            System.out.println("[GROQ] ========== REQUÊTE GROQ ==========");
            System.out.println("[GROQ] URL: " + API_URL);
            System.out.println("[GROQ] Titre demandé: " + forumTitle);
            System.out.println("[GROQ] ====================================");
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + API_KEY)
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
                System.err.println("[GROQ] Message: " + response.body());
                return "Erreur API Groq (code " + response.statusCode() + ")";
            }
        } catch (IOException e) {
            System.err.println("[GROQ] IOException: " + e.getMessage());
            e.printStackTrace();
            return "Erreur connexion: " + e.getMessage();
        } catch (InterruptedException e) {
            System.err.println("[GROQ] InterruptedException: " + e.getMessage());
            Thread.currentThread().interrupt();
            return "Requête annulée";
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
            e.printStackTrace();
        }
        return "Erreur: impossible d'extraire la réponse.";
    }
}
