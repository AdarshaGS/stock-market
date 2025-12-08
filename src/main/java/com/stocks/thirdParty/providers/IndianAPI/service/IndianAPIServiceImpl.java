package com.stocks.thirdParty.providers.IndianAPI.service;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.externalServices.data.ExternalServicePropertiesEntity;
import com.externalServices.service.ExternalService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stocks.exception.SymbolNotFoundException;
import com.stocks.thirdParty.ThirdPartyResponse;

@Service
public class IndianAPIServiceImpl implements IndianAPIService {

    private final HttpClient httpClient;
    private final ExternalService externalService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String SERVICE_NAME = "INDIANAPI";

    public IndianAPIServiceImpl(ExternalService externalService) {
        this.httpClient = HttpClient.newHttpClient();
        this.externalService = externalService;
    }

    @Override
    public ThirdPartyResponse fetchStockData(String symbol) {

        final List<ExternalServicePropertiesEntity> properties = this.externalService
                .getExternalServicePropertiesByServiceName(
                        SERVICE_NAME);
        Map<String, String> headers = constructHeaders(properties);

        String apiEndpoint = properties.stream()
                .filter(prop -> "endpoint".equalsIgnoreCase(prop.getName()))
                .map(ExternalServicePropertiesEntity::getValue)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("API endpoint not found"));

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(apiEndpoint + "?name=" + encodeSymbol(symbol)))
                .GET();

        headers.forEach(builder::header);

        HttpRequest request = builder.build();

        HttpResponse<String> httpResponse = this.httpClient
                .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .join();

        String response = httpResponse.body();

        ThirdPartyResponse thirdPartyResponse = null;
        try {
            thirdPartyResponse = objectMapper.readValue(response, ThirdPartyResponse.class);
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        if (thirdPartyResponse == null) {
            throw new SymbolNotFoundException("Symbol not found in third-party API: " + symbol);
        }
        return thirdPartyResponse;
    }

    private Map<String, String> constructHeaders(final List<ExternalServicePropertiesEntity> properties) {

        String apiKey = properties.stream()
                .filter(prop -> "x-api-key".equalsIgnoreCase(prop.getName()))
                .map(ExternalServicePropertiesEntity::getValue)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("API key not found"));

        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        headers.put("x-api-key", apiKey);
        return headers;
    }

    private String encodeSymbol(String symbol) {
        try {
            return URLEncoder.encode(symbol, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Failed to encode symbol", e);
        }
    }
}
