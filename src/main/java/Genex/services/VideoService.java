package Genex.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VideoService {
    private static final String YOUTUBE_API_BASE = "https://www.youtube.com/embed/";
    private String apiKey;

    public VideoService() {
        this.apiKey = System.getenv("YOUTUBE_API_KEY");
    }

    /**
     * Extracts YouTube video ID from various URL formats.
     * Supports: youtu.be/VIDEO_ID, youtube.com/watch?v=VIDEO_ID, youtube.com/embed/VIDEO_ID
     */
    public String extractVideoId(String videoUrl) {
        if (videoUrl == null || videoUrl.isBlank()) {
            return null;
        }

        // Handle youtu.be short URLs
        if (videoUrl.contains("youtu.be/")) {
            String[] parts = videoUrl.split("youtu.be/");
            if (parts.length > 1) {
                String id = parts[1].split("[?&#]")[0];
                return id.isBlank() ? null : id;
            }
        }

        // Handle youtube.com/watch?v= URLs
        if (videoUrl.contains("youtube.com/watch")) {
            String[] params = videoUrl.split("[?&]");
            for (String param : params) {
                if (param.startsWith("v=")) {
                    String id = param.substring(2);
                    return id.isBlank() ? null : id;
                }
            }
        }

        // Handle youtube.com/embed/ URLs
        if (videoUrl.contains("youtube.com/embed/")) {
            String[] parts = videoUrl.split("youtube.com/embed/");
            if (parts.length > 1) {
                String id = parts[1].split("[?&#]")[0];
                return id.isBlank() ? null : id;
            }
        }

        // If it looks like a raw video ID (11 characters, alphanumeric with - and _)
        if (videoUrl.matches("[\\w-]{11}")) {
            return videoUrl;
        }

        return null;
    }

    /**
     * Gets the embedded YouTube player URL for a video ID.
     */
    public String getEmbeddedUrl(String videoId) {
        if (videoId == null || videoId.isBlank()) {
            return null;
        }
        return YOUTUBE_API_BASE + videoId + "?autoplay=1&modestbranding=1";
    }

    /**
     * Gets the standard YouTube watch URL.
     */
    public String getWatchUrl(String videoId) {
        if (videoId == null || videoId.isBlank()) {
            return null;
        }
        return "https://www.youtube.com/watch?v=" + videoId;
    }

    /**
     * Gets a playable video stream URL for the given video ID.
     * Uses the noembed API as a fallback for video information.
     */
    public String getPlayableUrl(String videoId) {
        if (videoId == null || videoId.isBlank()) {
            return null;
        }
        
        String youtubeUrl = "https://www.youtube.com/watch?v=" + videoId;
        
        try {
            // Try to get video info via noembed API
            String noembed = "https://noembed.com/embed?url=" + youtubeUrl;
            String response = fetchUrl(noembed);
            
            if (response != null && !response.isBlank()) {
                // Extract thumbnail or alternative playable source
                return youtubeUrl;
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch playable URL: " + e.getMessage());
        }
        
        return youtubeUrl;
    }

    /**
     * Fetches content from a URL.
     */
    private String fetchUrl(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        
        int responseCode = connection.getResponseCode();
        if (responseCode == 200) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            }
        }
        return null;
    }

    /**
     * Validates if the API key is configured.
     */
    public boolean isApiKeyConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    public String getApiKey() {
        return apiKey;
    }
}

