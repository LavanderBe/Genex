package Genex.services;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemperatureService {

    private static final double TUNIS_LATITUDE = 36.8065;
    private static final double TUNIS_LONGITUDE = 10.1815;

    private static final Pattern TEMPERATURE_PATTERN = Pattern.compile("\"temperature_2m\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)");
    private static final Pattern WEATHER_CODE_PATTERN = Pattern.compile("\"weather_code\"\\s*:\\s*(\\d+)");

    private final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build();

    public TemperatureSnapshot getCurrentForTunis() throws IOException, InterruptedException {
        return getCurrentTemperature(TUNIS_LATITUDE, TUNIS_LONGITUDE);
    }

    public TemperatureSnapshot getCurrentTemperature(double latitude, double longitude) throws IOException, InterruptedException {
        String url = buildUrl(latitude, longitude);
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofSeconds(8))
            .GET()
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Réponse API inattendue: HTTP " + response.statusCode());
        }

        String body = response.body();
        double temperature = extractTemperature(body);
        int weatherCode = extractWeatherCode(body);
        String condition = weatherLabel(weatherCode);

        return new TemperatureSnapshot(temperature, condition, weatherCode);
    }

    private String buildUrl(double latitude, double longitude) {
        return "https://api.open-meteo.com/v1/forecast"
            + "?latitude=" + encode(String.valueOf(latitude))
            + "&longitude=" + encode(String.valueOf(longitude))
            + "&current=temperature_2m,weather_code"
            + "&timezone=auto";
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private double extractTemperature(String json) {
        Matcher matcher = TEMPERATURE_PATTERN.matcher(json);
        if (!matcher.find()) {
            throw new IllegalStateException("Température introuvable dans la réponse météo.");
        }
        return Double.parseDouble(matcher.group(1));
    }

    private int extractWeatherCode(String json) {
        Matcher matcher = WEATHER_CODE_PATTERN.matcher(json);
        if (!matcher.find()) {
            throw new IllegalStateException("Code météo introuvable dans la réponse météo.");
        }
        return Integer.parseInt(matcher.group(1));
    }

    private String weatherLabel(int code) {
        return switch (code) {
            case 0 -> "Ciel dégagé";
            case 1, 2 -> "Plutôt clair";
            case 3 -> "Nuageux";
            case 45, 48 -> "Brouillard";
            case 51, 53, 55 -> "Bruine";
            case 56, 57 -> "Bruine verglaçante";
            case 61, 63, 65 -> "Pluie";
            case 66, 67 -> "Pluie verglaçante";
            case 71, 73, 75, 77 -> "Neige";
            case 80, 81, 82 -> "Averses";
            case 85, 86 -> "Averses de neige";
            case 95 -> "Orage";
            case 96, 99 -> "Orage avec grêle";
            default -> "Conditions variables";
        };
    }

    public String formatForUi(TemperatureSnapshot snapshot) {
        return "🌡 Tunis: " + String.format(Locale.FRANCE, "%.1f", snapshot.temperatureC()) + "°C • " + snapshot.condition();
    }

    public String iconUrlForCode(int weatherCode) {
        if (weatherCode == 0 || weatherCode == 1) {
            return "https://cdn.jsdelivr.net/gh/twitter/twemoji@14.0.2/assets/72x72/2600.png";
        }
        if (weatherCode == 2 || weatherCode == 3) {
            return "https://cdn.jsdelivr.net/gh/twitter/twemoji@14.0.2/assets/72x72/2601.png";
        }
        if (weatherCode == 45 || weatherCode == 48) {
            return "https://cdn.jsdelivr.net/gh/twitter/twemoji@14.0.2/assets/72x72/1f32b.png";
        }
        if ((weatherCode >= 51 && weatherCode <= 67) || (weatherCode >= 80 && weatherCode <= 82)) {
            return "https://cdn.jsdelivr.net/gh/twitter/twemoji@14.0.2/assets/72x72/1f327.png";
        }
        if ((weatherCode >= 71 && weatherCode <= 77) || weatherCode == 85 || weatherCode == 86) {
            return "https://cdn.jsdelivr.net/gh/twitter/twemoji@14.0.2/assets/72x72/2744.png";
        }
        if (weatherCode == 95 || weatherCode == 96 || weatherCode == 99) {
            return "https://cdn.jsdelivr.net/gh/twitter/twemoji@14.0.2/assets/72x72/26c8.png";
        }
        return "https://cdn.jsdelivr.net/gh/twitter/twemoji@14.0.2/assets/72x72/1f324.png";
    }

    public record TemperatureSnapshot(double temperatureC, String condition, int weatherCode) {}
}
