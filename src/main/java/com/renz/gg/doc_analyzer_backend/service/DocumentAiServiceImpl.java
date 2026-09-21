package com.renz.gg.doc_analyzer_backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renz.gg.doc_analyzer_backend.dto.DocumentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class DocumentAiServiceImpl implements DocumentAiService {

    private final String apiKey;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public DocumentAiServiceImpl(
            @Value("${gemini.api-key}") String apiKey,
            ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.create();
    }

    @Override
    public DocumentResponse mapToHtml(MultipartFile file) {
        try {
            String base64Image = Base64.getEncoder().encodeToString(file.getBytes());
            String mimeType = (file.getContentType() != null) ? file.getContentType() : "image/jpeg";

            String prompt = """
                Eres un experto en maquetación web y OCR visual.
                Analiza la imagen adjunta y replica el documento exacto en código HTML5 con CSS inline simple.

                Reglas estrictas:
                1. Respeta el orden de los párrafos, títulos, listas y tablas.
                2. Si hay tablas, usa <table> con bordes inline (border: 1px solid #ccc; border-collapse: collapse;).
                3. Mantén alineaciones de texto y estilos.
                4. Devuelve XHTML válido wrapped en <!DOCTYPE html><html><head><meta charset="UTF-8"/></head><body>...</body></html>.
                5. Responde ÚNICAMENTE en formato JSON válido con las claves "documentTitle" y "htmlContent".
                """;

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of("text", prompt),
                                    Map.of("inline_data", Map.of(
                                            "mime_type", mimeType,
                                            "data", base64Image
                                    ))
                            ))
                    ),
                    "generationConfig", Map.of(
                            "response_mime_type", "application/json",
                            "temperature", 0.1
                    )
            );

            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent?key=" + apiKey;

            String rawResponse = restClient.post()
                    .uri(url)
                    .header("Content-Type", "application/json")
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            var rootNode = objectMapper.readTree(rawResponse);
            String jsonText = rootNode.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();

            if (jsonText.startsWith("```")) {
                jsonText = jsonText.replaceAll("^```[a-z]*\\n", "").replaceAll("\\n```$", "").trim();
            }

            return objectMapper.readValue(jsonText, DocumentResponse.class);

        } catch (Exception e) {
            throw new RuntimeException("Error procesando imagen con Gemini: " + e.getMessage(), e);
        }
    }
}