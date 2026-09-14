package com.renz.gg.doc_analyzer_backend.service;

import com.renz.gg.doc_analyzer_backend.dto.DocumentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.model.Media;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class DocumentAiServiceImpl implements DocumentAiService {
    private final ChatClient chatClient;

    @Override
    public DocumentResponse mapToHtml(MultipartFile file) {
        var output = new BeanOutputConverter<>(DocumentResponse.class);
        var prompt = """
            Eres un experto en maquetación web y OCR visual.
            Analiza la imagen adjunta y replica el documento exacto en código HTML5 con CSS inline simple.
        
            Reglas estrictas:
            1. Respeta el orden de los párrafos, títulos, listas y tablas.
            2. Si hay tablas, usa <table> con bordes inline (border: 1px solid #ccc; border-collapse: collapse;).
            3. Mantén alineaciones de texto (text-align: right/center/left) y estilos (bold, italic).
            4. Devuelve un XHTML 100% válido wrapped en <!DOCTYPE html><html><head><meta charset="UTF-8"/></head><body>...</body></html>.
            5. Cierra TODAS las etiquetas adecuadamente (ejemplo: <br/>, <img/>).
        
            {format}
            """;

        var media = new Media(
                MimeTypeUtils.parseMimeType(file.getContentType() != null ? file.getContentType() : "image/jpeg"),
                file.getResource()
        );

        return chatClient.prompt()
                .user(u -> u.text(prompt)
                        .param("format", output.getFormat())
                        .media(media))
                .call()
                .entity(output);
    }
}
