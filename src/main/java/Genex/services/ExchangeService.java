package Genex.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

/**
 * Free currency exchange rates via fawazahmed0/exchange-api.
 * No API key required. Base currency: TND.
 *
 * Primary URL  : https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies/{base}.json
 * Fallback URL : https://latest.currency-api.pages.dev/v1/currencies/{base}.json
 */
public class ExchangeService {

    private static final String PRIMARY  = "https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies/%s.json";
    private static final String FALLBACK = "https://latest.currency-api.pages.dev/v1/currencies/%s.json";

    // Popular currencies relevant to Tunisian gamers
    public static final List<String> POPULAR = List.of(
            "tnd", "usd", "eur", "gbp", "sar", "aed", "dzd", "mad",
            "try", "jpy", "cad", "chf", "cny", "btc", "eth"
    );

    public static final Map<String, String> LABELS = Map.ofEntries(
            Map.entry("tnd", "TND — Dinar Tunisien"),
            Map.entry("usd", "USD — Dollar Américain"),
            Map.entry("eur", "EUR — Euro"),
            Map.entry("gbp", "GBP — Livre Sterling"),
            Map.entry("sar", "SAR — Riyal Saoudien"),
            Map.entry("aed", "AED — Dirham Émirati"),
            Map.entry("dzd", "DZD — Dinar Algérien"),
            Map.entry("mad", "MAD — Dirham Marocain"),
            Map.entry("try", "TRY — Livre Turque"),
            Map.entry("jpy", "JPY — Yen Japonais"),
            Map.entry("cad", "CAD — Dollar Canadien"),
            Map.entry("chf", "CHF — Franc Suisse"),
            Map.entry("cny", "CNY — Yuan Chinois"),
            Map.entry("btc", "BTC — Bitcoin"),
            Map.entry("eth", "ETH — Ethereum")
    );

    /**
     * Fetches all exchange rates from the given base currency.
     * Returns a map of { currencyCode -> rate }.
     */
    public Map<String, Double> getRates(String baseCurrency) throws Exception {
        String base = baseCurrency.toLowerCase();
        String json = fetchJson(String.format(PRIMARY, base),
                                String.format(FALLBACK, base));
        return parseRates(json, base);
    }

    /**
     * Converts an amount from one currency to another.
     */
    public BigDecimal convert(double amount, String from, String to) throws Exception {
        Map<String, Double> rates = getRates(from.toLowerCase());
        Double rate = rates.get(to.toLowerCase());
        if (rate == null) throw new Exception("Taux introuvable pour " + to.toUpperCase());
        return BigDecimal.valueOf(amount * rate).setScale(4, RoundingMode.HALF_UP);
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private String fetchJson(String primaryUrl, String fallbackUrl) throws Exception {
        try {
            return get(primaryUrl);
        } catch (Exception e) {
            return get(fallbackUrl); // fallback
        }
    }

    private String get(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(8000);
        conn.setRequestProperty("Accept", "application/json");
        if (conn.getResponseCode() != 200)
            throw new Exception("HTTP " + conn.getResponseCode());
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            return sb.toString();
        }
    }

    /**
     * Minimal JSON parser — extracts the nested currency rates object.
     * JSON structure: { "date": "...", "{base}": { "usd": 0.32, "eur": 0.30, ... } }
     */
    private Map<String, Double> parseRates(String json, String base) {
        Map<String, Double> rates = new LinkedHashMap<>();
        // Find the base object: "tnd": { ... }
        String key = "\"" + base + "\"";
        int start = json.indexOf(key);
        if (start == -1) return rates;
        start = json.indexOf('{', start + key.length());
        if (start == -1) return rates;
        int end = json.indexOf('}', start);
        if (end == -1) return rates;
        String block = json.substring(start + 1, end);

        // Parse "code": value pairs
        String[] pairs = block.split(",");
        for (String pair : pairs) {
            pair = pair.trim();
            int colon = pair.indexOf(':');
            if (colon == -1) continue;
            String code = pair.substring(0, colon).trim().replace("\"", "");
            String val  = pair.substring(colon + 1).trim();
            try {
                rates.put(code, Double.parseDouble(val));
            } catch (NumberFormatException ignored) {}
        }
        return rates;
    }
}
