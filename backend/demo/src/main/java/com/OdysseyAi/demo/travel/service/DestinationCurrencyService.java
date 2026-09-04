package com.OdysseyAi.demo.travel.service;

import com.OdysseyAi.demo.travel.interfaces.CurrencyClient;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DestinationCurrencyService {

    private final CurrencyClient currencyClient;

    @Data
    @Builder
    public static class DestinationCurrencyInfo {
        private String countryName;
        private String currencyCode;
        private String currencySymbol;
        private BigDecimal petrolPricePerLiterLocal;
        private BigDecimal drivingCostPerKmLocal;
        private BigDecimal exchangeRateFromInr; // 1 INR = X local currency
        private BigDecimal exchangeRateToInr;   // 1 local currency = X INR
    }

    private static class CountryConfig {
        final String countryName;
        final String currencyCode;
        final String currencySymbol;
        final double petrolPriceLocal;     // retail pump price per liter in local currency
        final double drivingCostPerKmLocal;// fuel + operational wear per km in local currency
        final double fallbackRateFromInr;  // 1 INR = X local currency

        CountryConfig(String countryName, String currencyCode, String currencySymbol,
                      double petrolPriceLocal, double drivingCostPerKmLocal, double fallbackRateFromInr) {
            this.countryName = countryName;
            this.currencyCode = currencyCode;
            this.currencySymbol = currencySymbol;
            this.petrolPriceLocal = petrolPriceLocal;
            this.drivingCostPerKmLocal = drivingCostPerKmLocal;
            this.fallbackRateFromInr = fallbackRateFromInr;
        }
    }

    private static final Map<String, CountryConfig> COUNTRY_CONFIGS = new HashMap<>();
    private static final Map<String, String> CITY_TO_COUNTRY_KEY = new HashMap<>();

    static {
        // -------------------------------------------------------------
        // Country Configurations (Fuel prices & Driving Benchmarks)
        // -------------------------------------------------------------
        COUNTRY_CONFIGS.put("JAPAN", new CountryConfig(
                "Japan", "JPY", "¥", 175.0, 22.0, 1.82));
        COUNTRY_CONFIGS.put("UNITED STATES", new CountryConfig(
                "United States", "USD", "$", 0.95, 0.18, 0.0118));
        COUNTRY_CONFIGS.put("FRANCE", new CountryConfig(
                "France", "EUR", "€", 1.82, 0.22, 0.0110));
        COUNTRY_CONFIGS.put("GERMANY", new CountryConfig(
                "Germany", "EUR", "€", 1.80, 0.22, 0.0110));
        COUNTRY_CONFIGS.put("ITALY", new CountryConfig(
                "Italy", "EUR", "€", 1.85, 0.23, 0.0110));
        COUNTRY_CONFIGS.put("SPAIN", new CountryConfig(
                "Spain", "EUR", "€", 1.65, 0.20, 0.0110));
        COUNTRY_CONFIGS.put("UNITED KINGDOM", new CountryConfig(
                "United Kingdom", "GBP", "£", 1.45, 0.19, 0.0094));
        COUNTRY_CONFIGS.put("UNITED ARAB EMIRATES", new CountryConfig(
                "United Arab Emirates", "AED", "د.إ", 3.15, 0.40, 0.043));
        COUNTRY_CONFIGS.put("THAILAND", new CountryConfig(
                "Thailand", "THB", "฿", 38.5, 4.5, 0.43));
        COUNTRY_CONFIGS.put("SINGAPORE", new CountryConfig(
                "Singapore", "SGD", "S$", 2.85, 0.35, 0.016));
        COUNTRY_CONFIGS.put("INDONESIA", new CountryConfig(
                "Indonesia", "IDR", "Rp", 13500.0, 1600.0, 192.0));
        COUNTRY_CONFIGS.put("SWITZERLAND", new CountryConfig(
                "Switzerland", "CHF", "CHF", 1.85, 0.25, 0.0105));
        COUNTRY_CONFIGS.put("AUSTRALIA", new CountryConfig(
                "Australia", "AUD", "A$", 1.85, 0.24, 0.018));
        COUNTRY_CONFIGS.put("CANADA", new CountryConfig(
                "Canada", "CAD", "CA$", 1.55, 0.20, 0.016));
        COUNTRY_CONFIGS.put("TURKEY", new CountryConfig(
                "Turkey", "TRY", "₺", 43.0, 5.0, 0.40));
        COUNTRY_CONFIGS.put("INDIA", new CountryConfig(
                "India", "INR", "₹", 102.0, 14.0, 1.0));

        // -------------------------------------------------------------
        // Major Cities & Regions Keyword Mapping
        // -------------------------------------------------------------
        // Japan
        CITY_TO_COUNTRY_KEY.put("tokyo", "JAPAN");
        CITY_TO_COUNTRY_KEY.put("osaka", "JAPAN");
        CITY_TO_COUNTRY_KEY.put("kyoto", "JAPAN");
        CITY_TO_COUNTRY_KEY.put("sapporo", "JAPAN");
        CITY_TO_COUNTRY_KEY.put("fukuoka", "JAPAN");
        CITY_TO_COUNTRY_KEY.put("hiroshima", "JAPAN");

        // USA
        CITY_TO_COUNTRY_KEY.put("new york", "UNITED STATES");
        CITY_TO_COUNTRY_KEY.put("nyc", "UNITED STATES");
        CITY_TO_COUNTRY_KEY.put("los angeles", "UNITED STATES");
        CITY_TO_COUNTRY_KEY.put("san francisco", "UNITED STATES");
        CITY_TO_COUNTRY_KEY.put("las vegas", "UNITED STATES");
        CITY_TO_COUNTRY_KEY.put("miami", "UNITED STATES");
        CITY_TO_COUNTRY_KEY.put("chicago", "UNITED STATES");
        CITY_TO_COUNTRY_KEY.put("hawaii", "UNITED STATES");

        // France
        CITY_TO_COUNTRY_KEY.put("paris", "FRANCE");
        CITY_TO_COUNTRY_KEY.put("nice", "FRANCE");
        CITY_TO_COUNTRY_KEY.put("lyon", "FRANCE");
        CITY_TO_COUNTRY_KEY.put("marseille", "FRANCE");

        // UK
        CITY_TO_COUNTRY_KEY.put("london", "UNITED KINGDOM");
        CITY_TO_COUNTRY_KEY.put("edinburgh", "UNITED KINGDOM");
        CITY_TO_COUNTRY_KEY.put("manchester", "UNITED KINGDOM");

        // UAE
        CITY_TO_COUNTRY_KEY.put("dubai", "UNITED ARAB EMIRATES");
        CITY_TO_COUNTRY_KEY.put("abu dhabi", "UNITED ARAB EMIRATES");
        CITY_TO_COUNTRY_KEY.put("sharjah", "UNITED ARAB EMIRATES");

        // Thailand
        CITY_TO_COUNTRY_KEY.put("bangkok", "THAILAND");
        CITY_TO_COUNTRY_KEY.put("phuket", "THAILAND");
        CITY_TO_COUNTRY_KEY.put("pattaya", "THAILAND");
        CITY_TO_COUNTRY_KEY.put("chiang mai", "THAILAND");

        // Singapore
        CITY_TO_COUNTRY_KEY.put("singapore", "SINGAPORE");

        // Indonesia
        CITY_TO_COUNTRY_KEY.put("bali", "INDONESIA");
        CITY_TO_COUNTRY_KEY.put("jakarta", "INDONESIA");
        CITY_TO_COUNTRY_KEY.put("ubud", "INDONESIA");

        // Italy
        CITY_TO_COUNTRY_KEY.put("rome", "ITALY");
        CITY_TO_COUNTRY_KEY.put("venice", "ITALY");
        CITY_TO_COUNTRY_KEY.put("florence", "ITALY");
        CITY_TO_COUNTRY_KEY.put("milan", "ITALY");

        // Spain
        CITY_TO_COUNTRY_KEY.put("barcelona", "SPAIN");
        CITY_TO_COUNTRY_KEY.put("madrid", "SPAIN");
        CITY_TO_COUNTRY_KEY.put("seville", "SPAIN");

        // Germany
        CITY_TO_COUNTRY_KEY.put("berlin", "GERMANY");
        CITY_TO_COUNTRY_KEY.put("munich", "GERMANY");
        CITY_TO_COUNTRY_KEY.put("frankfurt", "GERMANY");

        // Switzerland
        CITY_TO_COUNTRY_KEY.put("zurich", "SWITZERLAND");
        CITY_TO_COUNTRY_KEY.put("geneva", "SWITZERLAND");
        CITY_TO_COUNTRY_KEY.put("lucerne", "SWITZERLAND");
        CITY_TO_COUNTRY_KEY.put("interlaken", "SWITZERLAND");
    }

    public DestinationCurrencyInfo resolveDestination(String destinationQuery) {
        if (destinationQuery == null || destinationQuery.isBlank()) {
            return getDefaultIndiaConfig();
        }

        String queryLower = destinationQuery.trim().toLowerCase(Locale.ROOT);
        CountryConfig config = null;

        // 1. Check city keywords
        for (Map.Entry<String, String> entry : CITY_TO_COUNTRY_KEY.entrySet()) {
            if (queryLower.contains(entry.getKey())) {
                config = COUNTRY_CONFIGS.get(entry.getValue());
                break;
            }
        }

        // 2. Check country keywords
        if (config == null) {
            for (Map.Entry<String, CountryConfig> entry : COUNTRY_CONFIGS.entrySet()) {
                if (queryLower.contains(entry.getKey().toLowerCase(Locale.ROOT))
                        || queryLower.contains(entry.getValue().countryName.toLowerCase(Locale.ROOT))) {
                    config = entry.getValue();
                    break;
                }
            }
        }

        // Default to India if not matched
        if (config == null) {
            config = COUNTRY_CONFIGS.get("INDIA");
        }

        // 3. Resolve live exchange rate from Frankfurter API
        BigDecimal rateFromInr = BigDecimal.valueOf(config.fallbackRateFromInr);
        BigDecimal rateToInr = BigDecimal.ONE;

        if (!"INR".equalsIgnoreCase(config.currencyCode)) {
            try {
                BigDecimal liveRate = currencyClient.getExchangeRate("INR", config.currencyCode);
                if (liveRate != null && liveRate.compareTo(BigDecimal.ZERO) > 0) {
                    rateFromInr = liveRate;
                }
            } catch (Exception ex) {
                log.warn("Frankfurter API rate lookup failed for INR to {}, using fallback {}: {}",
                        config.currencyCode, config.fallbackRateFromInr, ex.getMessage());
            }

            if (rateFromInr.compareTo(BigDecimal.ZERO) > 0) {
                rateToInr = BigDecimal.ONE.divide(rateFromInr, 6, RoundingMode.HALF_UP);
            }
        }

        return DestinationCurrencyInfo.builder()
                .countryName(config.countryName)
                .currencyCode(config.currencyCode)
                .currencySymbol(config.currencySymbol)
                .petrolPricePerLiterLocal(BigDecimal.valueOf(config.petrolPriceLocal))
                .drivingCostPerKmLocal(BigDecimal.valueOf(config.drivingCostPerKmLocal))
                .exchangeRateFromInr(rateFromInr)
                .exchangeRateToInr(rateToInr)
                .build();
    }

    private DestinationCurrencyInfo getDefaultIndiaConfig() {
        CountryConfig config = COUNTRY_CONFIGS.get("INDIA");
        return DestinationCurrencyInfo.builder()
                .countryName(config.countryName)
                .currencyCode(config.currencyCode)
                .currencySymbol(config.currencySymbol)
                .petrolPricePerLiterLocal(BigDecimal.valueOf(config.petrolPriceLocal))
                .drivingCostPerKmLocal(BigDecimal.valueOf(config.drivingCostPerKmLocal))
                .exchangeRateFromInr(BigDecimal.ONE)
                .exchangeRateToInr(BigDecimal.ONE)
                .build();
    }
}
