package Genex.services;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Properties;

/**
 * Service for generating images using Google Imagen API (via Vertex AI)
 * Alternative: Uses Pollinations.ai FREE API (no key required)
 * FREE TIER: Unlimited generations with Pollinations.ai
 */
public class GeminiImageGeneratorService {

    private static final String API_KEY_FILE = "src/main/resources/gemini-config.properties";
    // Using Pollinations.ai - FREE, no API key required
    private static final String POLLINATIONS_API_URL = "https://image.pollinations.ai/prompt/";
    private static final boolean USE_FREE_API = true; // Set to true to use free API
    private static final int TIMEOUT_MS = 60000; // 60 seconds timeout
    private static final int MAX_RETRIES = 2; // Retry up to 2 times
    
    private String apiKey;
    private Gson gson;

    public GeminiImageGeneratorService() {
        this.gson = new Gson();
        if (!USE_FREE_API) {
            loadApiKey();
        } else {
            System.out.println("✅ Using FREE Pollinations.ai API (no key required)");
        }
    }

    /**
     * Load API key from config file (only if not using free API)
     */
    private void loadApiKey() {
        try {
            File configFile = new File(API_KEY_FILE);
            if (!configFile.exists()) {
                System.err.println("⚠️ Gemini config file not found: " + API_KEY_FILE);
                return;
            }

            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(configFile)) {
                props.load(fis);
                this.apiKey = props.getProperty("gemini.api.key");
                
                if (apiKey == null || apiKey.trim().isEmpty()) {
                    System.err.println("⚠️ API key not found in config file");
                } else {
                    System.out.println("✅ Gemini API key loaded successfully");
                }
            }
        } catch (IOException e) {
            System.err.println("❌ Error loading Gemini API key: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Generate a team logo using AI
     * @param teamName Name of the team
     * @param gameName Name of the game (e.g., "Valorant", "League of Legends")
     * @param customDescription Optional custom description from user (can be empty)
     * @return Path to the generated image file, or null if failed
     */
    public String generateTeamLogo(String teamName, String gameName, String customDescription) {
        String prompt;
        
        if (customDescription != null && !customDescription.trim().isEmpty()) {
            // Use custom description if provided
            prompt = String.format(
                "professional esports team logo for %s playing %s. %s. " +
                "High quality, 1024x1024, modern design, vibrant colors",
                teamName, gameName, customDescription.trim()
            );
        } else {
            // Use default prompt
            prompt = String.format(
                "professional esports team logo for %s playing %s, " +
                "modern bold design, stylized text, vibrant gaming colors, " +
                "digital art, vector style, clean, high quality",
                teamName, gameName
            );
        }

        System.out.println("🎨 Generating team logo for: " + teamName);
        if (customDescription != null && !customDescription.trim().isEmpty()) {
            System.out.println("📝 Custom description: " + customDescription);
        }
        return generateImageWithFreeAPI(prompt, "logo", teamName);
    }

    /**
     * Generate a team jersey design using AI
     * @param teamName Name of the team
     * @param gameName Name of the game
     * @param customDescription Optional custom description from user (can be empty)
     * @return Path to the generated image file, or null if failed
     */
    public String generateTeamJersey(String teamName, String gameName, String customDescription) {
        String prompt;
        
        if (customDescription != null && !customDescription.trim().isEmpty()) {
    prompt = String.format(
        "PRODUCT PHOTOGRAPHY ONLY: A single esports jersey garment for team '%s' (%s), " +
        "completely flat lay on a pure white surface, NO body, NO hands, NO arms, NO person, NO mannequin, NO model. " +
        "The jersey is empty with zero human presence. " +
        "Design: %s. " +
        "athletic polyester cut, sublimation print, team name '%s' boldly printed on chest. " +
        "Shot from directly above (top-down bird's eye view), perfectly centered, " +
        "soft even studio lighting, no shadows, no wrinkles, isolated on white background. " +
        "Style: professional apparel product catalog photo, 1024x1024.",
        teamName, gameName, customDescription.trim(), teamName
    );
} else {
    prompt = String.format(
        "PRODUCT PHOTOGRAPHY ONLY: A single esports jersey garment for team '%s' (%s), " +
        "completely flat lay on a pure white surface, NO body, NO hands, NO arms, NO person, NO mannequin, NO model. " +
        "The jersey is empty with zero human presence. " +
        "athletic polyester fit, sublimation print design. " +
        "Bold geometric patterns with sharp angular lines, dynamic gradient (deep blue to electric purple to vivid orange and cyan), " +
        "digital camo texture layered with glowing circuit-board or hex-grid overlay, " +
        "team name '%s' printed in bold blocky esports font centered on chest, accent trim on collar and sleeves. " +
        "Shot from directly above (top-down bird's eye view), perfectly centered, " +
        "soft even studio lighting, no shadows, no wrinkles, isolated on white background. " +
        "Style: professional apparel product catalog photo, 1024x1024.",
        teamName, gameName, teamName
    );
}
        System.out.println("👕 Generating team jersey for: " + teamName);
        if (customDescription != null && !customDescription.trim().isEmpty()) {
            System.out.println("📝 Custom description: " + customDescription);
        }
        return generateImageWithFreeAPI(prompt, "jersey", teamName);
    }

    /**
     * Generate image using FREE Pollinations.ai API
     * @param prompt The text prompt describing the image
     * @param type Type of image ("logo" or "jersey")
     * @param teamName Team name for file naming
     * @return Path to the generated image file, or null if failed
     */
    private String generateImageWithFreeAPI(String prompt, String type, String teamName) {
        // Try multiple times in case of timeout
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                System.out.println("📤 Attempt " + attempt + "/" + MAX_RETRIES + " - Requesting image from Pollinations.ai...");
                
                // URL encode the prompt
                String encodedPrompt = java.net.URLEncoder.encode(prompt, "UTF-8");
                
                // Build URL with parameters for better quality
                String urlString = POLLINATIONS_API_URL + encodedPrompt + 
                                  "?width=1024&height=1024&nologo=true&enhance=true";
                
                System.out.println("🎯 Prompt: " + prompt);
                
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                conn.setConnectTimeout(TIMEOUT_MS); // 60 seconds
                conn.setReadTimeout(TIMEOUT_MS); // 60 seconds
                
                int responseCode = conn.getResponseCode();
                System.out.println("📥 Response code: " + responseCode);
                
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Read image data
                    InputStream inputStream = conn.getInputStream();
                    BufferedImage image = ImageIO.read(inputStream);
                    inputStream.close();
                    
                    if (image != null) {
                        // Save image to file
                        String savedPath = saveImage(image, type, teamName);
                        if (savedPath != null) {
                            System.out.println("✅ Image generated and saved successfully!");
                            return savedPath;
                        }
                    } else {
                        System.err.println("❌ Failed to read image from response");
                    }
                } else {
                    System.err.println("❌ API returned error code: " + responseCode);
                }
                
            } catch (java.net.SocketTimeoutException e) {
                System.err.println("⏱️ Timeout on attempt " + attempt + "/" + MAX_RETRIES);
                if (attempt < MAX_RETRIES) {
                    System.out.println("🔄 Retrying in 2 seconds...");
                    try {
                        Thread.sleep(2000); // Wait 2 seconds before retry
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    System.err.println("❌ All retry attempts failed due to timeout");
                }
            } catch (Exception e) {
                System.err.println("❌ Error generating image: " + e.getMessage());
                e.printStackTrace();
                break; // Don't retry on other errors
            }
        }
        
        return null;
    }

    /**
     * Save BufferedImage to file
     */
    private String saveImage(BufferedImage image, String type, String teamName) {
        try {
            // Create directory
            Path dir = Paths.get("uploads", "team-" + type + "s");
            Files.createDirectories(dir);

            // Generate filename
            String sanitizedName = teamName.replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();
            String fileName = System.currentTimeMillis() + "_" + sanitizedName + "_" + type + ".png";
            Path filePath = dir.resolve(fileName);

            // Save image
            File outputFile = filePath.toFile();
            ImageIO.write(image, "png", outputFile);

            String relativePath = "uploads/team-" + type + "s/" + fileName;
            System.out.println("✅ Image saved: " + relativePath);
            return relativePath;

        } catch (Exception e) {
            System.err.println("❌ Error saving image: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Check if API is ready to use
     */
    public boolean isConfigured() {
        return USE_FREE_API || (apiKey != null && !apiKey.trim().isEmpty());
    }
}
