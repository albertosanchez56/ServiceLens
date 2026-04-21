package com.servicelens.platform.detection;

import java.net.URI;
import java.util.OptionalDouble;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.annotation.PostConstruct;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Cliente mínimo para {@code GET /api/v1/query} (instant query).
 */
@Component
public class PrometheusClient {

    private static final Logger log = LoggerFactory.getLogger(PrometheusClient.class);

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;

    public PrometheusClient(
            @Value("${servicelens.prometheus.base-url:http://localhost:9090}") String baseUrl,
            ObjectMapper objectMapper) {
        this.baseUrl = normalizeBaseUrl(baseUrl);
        // Sin baseUrl en el builder: la URI absoluta evita ambigüedades al combinar host + path + query.
        this.restClient = RestClient.builder().build();
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    void logTarget() {
        log.info("PrometheusClient usará baseUrl={}", baseUrl);
    }

    /**
     * Devuelve el valor escalar del primer resultado, o vacío si no hay serie.
     */
    public OptionalDouble queryScalar(String promql) {
        URI queryUri = buildQueryUri(promql);
        try {
            String body = restClient.get().uri(queryUri).retrieve().body(String.class);
            if (body == null || body.isBlank()) {
                log.warn("Prometheus devolvió cuerpo vacío (uri={})", queryUri);
                return OptionalDouble.empty();
            }
            return parseQueryResponseBody(body);
        } catch (RestClientException e) {
            log.warn("No se pudo consultar Prometheus (uri={}): {}", queryUri, e.getMessage());
            return OptionalDouble.empty();
        } catch (Exception e) {
            log.warn("Error inesperado consultando Prometheus (uri={}): {}", queryUri, e.getMessage(), e);
            return OptionalDouble.empty();
        }
    }

    private URI buildQueryUri(String promql) {
        return UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/api/v1/query")
                .queryParam("query", promql)
                .build()
                .toUri();
    }

    private OptionalDouble parseQueryResponseBody(String body) {
        try {
            JsonNode root = objectMapper.readTree(body);
            if (!"success".equals(root.path("status").asText())) {
                log.warn(
                        "Prometheus respondió sin success: error={} errorType={} bodySnippet={}",
                        root.path("error").asText(),
                        root.path("errorType").asText(),
                        truncate(body, 400));
                return OptionalDouble.empty();
            }
            JsonNode data = root.path("data");
            String resultType = data.path("resultType").asText();
            JsonNode result = data.path("result");
            // PromQL escalar: resultType=scalar, result es [timestamp, "valor"]
            if ("scalar".equals(resultType) && result.isArray() && result.size() >= 2) {
                return parseNumericValue(result.get(1));
            }
            if (!result.isArray() || result.isEmpty()) {
                return OptionalDouble.of(0.0);
            }
            // Vector: primera serie con metric + value
            JsonNode value = result.get(0).path("value");
            if (!value.isArray() || value.size() < 2) {
                if (result.size() >= 2 && result.get(0).isNumber() && result.get(1).isTextual()) {
                    return parseNumericValue(result.get(1));
                }
                log.warn(
                        "Formato de vector Prometheus inesperado (resultType={}): snippet={}",
                        resultType,
                        truncate(body, 500));
                return OptionalDouble.empty();
            }
            return parseNumericValue(value.get(1));
        } catch (Exception e) {
            log.warn(
                    "Error parseando JSON de Prometheus: {} snippet={}",
                    e.getMessage(),
                    truncate(body, 500));
            return OptionalDouble.empty();
        }
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        return s.length() <= max ? s : s.substring(0, max) + "…";
    }

    private static String normalizeBaseUrl(String raw) {
        String s = raw == null ? "" : raw.trim();
        if (s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"")) {
            s = s.substring(1, s.length() - 1).trim();
        }
        while (s.endsWith("/")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    private static OptionalDouble parseNumericValue(JsonNode node) {
        if (node == null || node.isNull()) {
            return OptionalDouble.of(0.0);
        }
        if (node.isNumber()) {
            return OptionalDouble.of(node.asDouble());
        }
        String s = node.asText();
        if (s == null || s.isEmpty() || "NaN".equals(s)) {
            return OptionalDouble.of(0.0);
        }
        try {
            return OptionalDouble.of(Double.parseDouble(s));
        } catch (NumberFormatException e) {
            log.warn("Valor numérico Prometheus no parseable: [{}]", s);
            return OptionalDouble.empty();
        }
    }
}
