package com.renz.gg.doc_analyzer_backend.dto;

public record DocumentResponse(
        String documentTitle,
        String htmlContent
) {
}
