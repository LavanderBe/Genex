package Genex.services;

import Genex.entities.Forum;
import Genex.entities.Posts;
import Genex.entities.User;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class TelegramNotificationService {

    private static final String TELEGRAM_API_TEMPLATE = "https://api.telegram.org/bot%s/sendMessage";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final String botToken;
    private final String chatId;

    public TelegramNotificationService() {
        Properties localConfig = loadLocalConfig();
        this.botToken = resolveValue("TELEGRAM_BOT_TOKEN", "telegram.bot.token", localConfig);
        this.chatId = resolveValue("TELEGRAM_CHAT_ID", "telegram.chat.id", localConfig);
    }

    public void sendForumCreatedNotification(Forum forum, User currentUser) throws IOException, InterruptedException {
        if (isBlank(botToken) || isBlank(chatId)) {
            throw new IllegalStateException("Variables d'environnement TELEGRAM_BOT_TOKEN et TELEGRAM_CHAT_ID requises.");
        }

        String message = buildForumCreatedMessage(forum, currentUser);

        JsonObject body = new JsonObject();
        body.addProperty("chat_id", chatId);
        body.addProperty("text", message);
        body.addProperty("parse_mode", "HTML");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TELEGRAM_API_TEMPLATE.formatted(botToken)))
                .timeout(Duration.ofSeconds(8))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Telegram API HTTP " + response.statusCode() + ": " + response.body());
        }

        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        if (!json.has("ok") || !json.get("ok").getAsBoolean()) {
            String apiMessage = json.has("description") ? json.get("description").getAsString() : "Réponse Telegram invalide.";
            throw new IOException("Telegram API error: " + apiMessage);
        }
    }

    public void sendForumDeletedNotification(Forum forum, User currentUser) throws IOException, InterruptedException {
        ensureConfigured();
        String message = buildForumDeletedMessage(forum, currentUser);
        sendMessage(message);
    }

    public void sendPlayerPostCreatedNotification(Posts post, User currentUser, String forumName) throws IOException, InterruptedException {
        ensureConfigured();
        String message = buildPlayerPostCreatedMessage(post, currentUser, forumName);
        sendMessage(message);
    }

    private String buildForumCreatedMessage(Forum forum, User currentUser) {
        String actor = currentUser == null || isBlank(currentUser.getUsername()) ? safe(forum.getCreatedBy()) : currentUser.getUsername().trim();
        String forumTitle = safe(forum.getTitle()).trim();
        String description = safe(forum.getDescription()).trim();
        String createdAt = forum.getCreatedAt() == null ? "-" : forum.getCreatedAt().format(DATE_FORMATTER);

        StringBuilder message = new StringBuilder();
        message.append("📣 <b>Nouveau forum créé</b>\n\n");
        message.append("🧩 <b>Titre:</b> ").append(escapeHtml(forumTitle)).append("\n");
        message.append("👤 <b>Admin:</b> ").append(escapeHtml(actor)).append("\n");
        message.append("🕒 <b>Date:</b> ").append(escapeHtml(createdAt));

        if (!description.isBlank()) {
            message.append("\n\n📝 <b>Description:</b>\n").append(escapeHtml(description));
        }
        return message.toString();
    }

    private String buildForumDeletedMessage(Forum forum, User currentUser) {
        String actor = currentUser == null || isBlank(currentUser.getUsername()) ? safe(forum.getCreatedBy()) : currentUser.getUsername().trim();
        String forumTitle = safe(forum.getTitle()).trim();
        String description = safe(forum.getDescription()).trim();
        String deletedAt = DATE_FORMATTER.format(java.time.LocalDateTime.now());

        StringBuilder message = new StringBuilder();
        message.append("🗑️ <b>Forum supprimé</b>\n\n");
        message.append("⚠️ <b>Action admin détectée</b>\n");
        message.append("🧩 <b>Forum:</b> ").append(escapeHtml(forumTitle)).append("\n");
        message.append("👤 <b>Supprimé par:</b> ").append(escapeHtml(actor)).append("\n");
        message.append("🕒 <b>Heure:</b> ").append(escapeHtml(deletedAt));
        if (!description.isBlank()) {
            message.append("\n\n🧾 <b>Ancienne description:</b>\n").append(escapeHtml(description));
        }
        return message.toString();
    }

    private String buildPlayerPostCreatedMessage(Posts post, User currentUser, String forumName) {
        String player = currentUser == null || isBlank(currentUser.getUsername()) ? safe(post.getAuthorId()) : currentUser.getUsername().trim();
        String title = safe(post.getTitle()).trim();
        String body = safe(post.getBody()).trim();
        String tag = safe(post.getTag()).trim();
        String status = safe(post.getPostStatus()).trim();
        String type = safe(post.getPostType()).trim();
        String createdAt = post.getCreatedAt() == null ? DATE_FORMATTER.format(java.time.LocalDateTime.now()) : post.getCreatedAt().format(DATE_FORMATTER);

        StringBuilder message = new StringBuilder();
        message.append("🚀 <b>NOUVELLE PUBLICATION PLAYER</b> 🚀\n\n");
        message.append("🎮 <b>Player:</b> ").append(escapeHtml(player)).append("\n");
        message.append("🏟️ <b>Forum:</b> ").append(escapeHtml(forumName)).append("\n");
        message.append("🧠 <b>Titre:</b> ").append(escapeHtml(title)).append("\n");
        message.append("📌 <b>Type:</b> ").append(escapeHtml(type.isBlank() ? "DISCUSSION" : type)).append(" • <b>Statut:</b> ").append(escapeHtml(status.isBlank() ? "ACTIF" : status)).append("\n");
        message.append("🕒 <b>Heure:</b> ").append(escapeHtml(createdAt));
        if (!tag.isBlank()) {
            message.append("\n🏷️ <b>Tag:</b> ").append(escapeHtml(tag));
        }
        if (!body.isBlank()) {
            String preview = body.length() > 260 ? body.substring(0, 260).trim() + "…" : body;
            message.append("\n\n💬 <b>Aperçu:</b>\n").append(escapeHtml(preview));
        }
        message.append("\n\n✨ <i>Une nouvelle discussion est en train de monter !</i>");
        return message.toString();
    }

    private void ensureConfigured() {
        if (isBlank(botToken) || isBlank(chatId)) {
            throw new IllegalStateException("Variables d'environnement TELEGRAM_BOT_TOKEN et TELEGRAM_CHAT_ID requises.");
        }
    }

    private void sendMessage(String message) throws IOException, InterruptedException {
        JsonObject body = new JsonObject();
        body.addProperty("chat_id", chatId);
        body.addProperty("text", message);
        body.addProperty("parse_mode", "HTML");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TELEGRAM_API_TEMPLATE.formatted(botToken)))
                .timeout(Duration.ofSeconds(8))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Telegram API HTTP " + response.statusCode() + ": " + response.body());
        }

        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        if (!json.has("ok") || !json.get("ok").getAsBoolean()) {
            String apiMessage = json.has("description") ? json.get("description").getAsString() : "Réponse Telegram invalide.";
            throw new IOException("Telegram API error: " + apiMessage);
        }
    }

    private String escapeHtml(String value) {
        return safe(value)
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private Properties loadLocalConfig() {
        Properties properties = new Properties();
        try (InputStream input = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("telegram.local.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Impossible de charger telegram.local.properties: " + e.getMessage(), e);
        }
        return properties;
    }

    private String resolveValue(String envKey, String propertyKey, Properties localConfig) {
        String fromEnv = System.getenv(envKey);
        if (!isBlank(fromEnv)) {
            return fromEnv;
        }
        String fromSystemProperty = System.getProperty(propertyKey);
        if (!isBlank(fromSystemProperty)) {
            return fromSystemProperty;
        }
        return localConfig.getProperty(propertyKey);
    }
}
