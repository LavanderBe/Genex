package Genex.utils;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class HcaptchaVerifier {
    private static final String SECRET_KEY = "0xYOUR_SECRET_KEY_HERE"; // ← Change this!

    public static boolean verify(String token, String remoteIp) {
        if (token == null || token.isBlank()) return false;

        try {
            String params = "response=" + URLEncoder.encode(token, StandardCharsets.UTF_8) +
                    "&secret=" + URLEncoder.encode(SECRET_KEY, StandardCharsets.UTF_8);

            if (remoteIp != null && !remoteIp.isBlank()) {
                params += "&remoteip=" + URLEncoder.encode(remoteIp, StandardCharsets.UTF_8);
            }

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.hcaptcha.com/siteverify"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(params))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Simple check (you can use Gson for better JSON parsing)
            return response.body().contains("\"success\":true");

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
