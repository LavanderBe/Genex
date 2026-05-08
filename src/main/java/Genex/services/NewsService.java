package Genex.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class NewsService {

    private static final int MAX_TITLE_LENGTH = 110;

    private final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build();

    public NewsItem getLatestGamingNews() throws IOException, InterruptedException {
        List<NewsItem> feed = getGamingFeed(1);
        if (feed.isEmpty()) {
            throw new IllegalStateException("Aucune actualité trouvée.");
        }
        return feed.getFirst();
    }

    public List<NewsItem> getGamingFeed(int limit) throws IOException, InterruptedException {
        int requested = Math.max(1, Math.min(limit, 12));
        String query = encode("gaming");
        String url = "https://www.reddit.com/r/gaming/hot.json?limit=" + requested + "&q=" + query;
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofSeconds(8))
            .header("User-Agent", "GenexForum/1.0")
            .GET()
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Réponse API news inattendue: HTTP " + response.statusCode());
        }

        JsonObject root = JsonParser.parseString(response.body()).getAsJsonObject();
        JsonObject data = root.getAsJsonObject("data");
        if (data == null || !data.has("children")) {
            throw new IllegalStateException("Flux d'actualités invalide.");
        }

        JsonArray children = data.getAsJsonArray("children");
        List<NewsItem> items = new ArrayList<>();
        for (JsonElement childElement : children) {
            if (items.size() >= requested || childElement == null || !childElement.isJsonObject()) {
                continue;
            }
            JsonObject child = childElement.getAsJsonObject();
            JsonObject post = child.getAsJsonObject("data");
            if (post == null) {
                continue;
            }

            String title = stringValue(post, "title");
            if (title.isBlank()) {
                continue;
            }

            String source = stringValue(post, "subreddit_name_prefixed");
            if (source.isBlank()) {
                source = "r/gaming";
            }

            String imageUrl = extractImageUrl(post);
            String link = extractArticleUrl(post);
            items.add(new NewsItem(title, source, imageUrl, link));
        }

        if (items.isEmpty()) {
            throw new IllegalStateException("Aucune actualité exploitable trouvée.");
        }
        return items;
    }

    public String formatForUi(NewsItem item) {
        return "News: " + shorten(item.title());
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String extractImageUrl(JsonObject post) {
        String thumbnail = stringValue(post, "thumbnail");
        if (thumbnail.startsWith("http://") || thumbnail.startsWith("https://")) {
            return sanitizeUrl(thumbnail);
        }

        if (post.has("preview") && post.get("preview").isJsonObject()) {
            JsonObject preview = post.getAsJsonObject("preview");
            if (preview.has("images") && preview.get("images").isJsonArray()) {
                JsonArray images = preview.getAsJsonArray("images");
                if (images.size() > 0 && images.get(0).isJsonObject()) {
                    JsonObject first = images.get(0).getAsJsonObject();
                    if (first.has("source") && first.get("source").isJsonObject()) {
                        JsonObject source = first.getAsJsonObject("source");
                        String previewUrl = stringValue(source, "url");
                        if (!previewUrl.isBlank()) {
                            return sanitizeUrl(previewUrl);
                        }
                    }
                }
            }
        }
        return "";
    }

    private String extractArticleUrl(JsonObject post) {
        String url = sanitizeUrl(stringValue(post, "url"));
        if (!url.isBlank()) {
            return url;
        }
        String permalink = stringValue(post, "permalink");
        if (!permalink.isBlank()) {
            return "https://www.reddit.com" + permalink;
        }
        return "";
    }

    private String sanitizeUrl(String value) {
        return value
            .replace("\\/", "/")
            .replace("&amp;", "&")
            .trim();
    }

    private String stringValue(JsonObject obj, String key) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) {
            return "";
        }
        return obj.get(key).getAsString().trim();
    }

    private String shorten(String text) {
        if (text.length() <= MAX_TITLE_LENGTH) {
            return text;
        }
        return text.substring(0, MAX_TITLE_LENGTH - 1).trim() + "…";
    }

    public record NewsItem(String title, String source, String imageUrl, String link) {}
}
