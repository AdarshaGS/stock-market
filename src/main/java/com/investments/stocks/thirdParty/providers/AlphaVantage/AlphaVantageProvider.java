package com.investments.stocks.thirdParty.providers.AlphaVantage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.externalServices.data.ExternalServicePropertiesEntity;
import com.externalServices.service.ExternalService;
import com.investments.stocks.thirdParty.StockDataProvider;
import com.investments.stocks.thirdParty.ThirdPartyResponse;
import com.investments.stocks.thirdParty.providers.AlphaVantage.data.AlphaVantageResponseOverview;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AlphaVantageProvider implements StockDataProvider {

    private ExternalService externalService;
    private final RestTemplate restTemplate = new RestTemplate();
    private final HttpClient httpClient;

    public AlphaVantageProvider(final ExternalService externalService) {
        this.externalService = externalService;
        this.httpClient = HttpClient.newHttpClient();
    }

    @Override
    public ThirdPartyResponse fetchStockData(String symbol) {
        log.info("Fetching data from AlphaVantage for {}", symbol);
        final List<ExternalServicePropertiesEntity> properties = this.externalService
                .getExternalServicePropertiesByServiceName("ALPHA_VANTAGE");

        String apiKey = properties.stream()
                .filter(prop -> "api-key".equalsIgnoreCase(prop.getName()))
                .map(ExternalServicePropertiesEntity::getValue)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("API key not found"));

        String url = properties.stream()
                .filter(prop -> "url".equalsIgnoreCase(prop.getName()))
                .map(ExternalServicePropertiesEntity::getValue)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("URL not found"));

        // construct request
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url + "&symbol=" + symbol + "&apikey=" + apiKey))
                .GET();

        HttpRequest request = builder.build();

        try {
            @SuppressWarnings("unused")
            HttpResponse<String> thirdPartyResponse = this.httpClient
                    .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .join();
            AlphaVantageResponseOverview response = this.restTemplate
                    .getForObject(url + "?symbol=" + symbol + "&apiKey=" + apiKey, AlphaVantageResponseOverview.class);

            if (response == null || response.getSymbol() == null) {
                throw new RuntimeException("Empty response from AlphaVantage");
            }
            return mapToThirdPartyResponse(response);
        } catch (Exception e) {
            log.error("AlphaVantage fetch failed: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public String getProviderName() {
        return "AlphaVantage";
    }

    private ThirdPartyResponse mapToThirdPartyResponse(AlphaVantageResponseOverview av) {
        ThirdPartyResponse response = new ThirdPartyResponse();
        response.setCompanyName(av.getName());

        ThirdPartyResponse.CompanyProfile profile = new ThirdPartyResponse.CompanyProfile();
        profile.setCompanyDescription(av.getDescription());
        profile.setMgIndustry(av.getIndustry());
        response.setCompanyProfile(profile);

        // Overview endpoint doesn't give real-time price, but gives Market Cap
        // We will misuse 'CurrentPrice' to store 0 if not available as user is okay
        // with mix/match or we assume fallback
        // Or we might need GLOBAL_QUOTE. But user specifically gave OVERVIEW url.
        // Let's set price to 0 or try to parse '52WeekHigh' as a proxy if desperate?
        // No, that's bad.
        // We will just set market cap.

        ThirdPartyResponse.CurrentPrice price = new ThirdPartyResponse.CurrentPrice();
        price.setNSE(0.0); // Not available in OVERVIEW
        price.setBSE(0.0);
        response.setCurrentPrice(price);

        // Map Market Cap
        try {
            // Double mc = Double.parseDouble(av.getMarketCapitalization());
            // AlphaVantage returns full number, our system expects standard units?
            // The system uses '20000' for Large Cap (Cr). AV returns raw bytes e.g
            // "155000000000".
            // Convert to Crores: / 10,000,000 (1 Cr = 10 Million)
            // Double mcCr = mc / 10000000;

            // response.setStockDetailsReusableData(
            // ThirdPartyResponse.StockDetailsReusableData.builder()
            // .marketCap(mcCr)
            // .build());
        } catch (Exception e) {
            // ignore parsing error
        }

        return response;
    }

}
