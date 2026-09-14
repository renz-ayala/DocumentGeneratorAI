package com.renz.gg.doc_analyzer_backend.service;

import com.renz.gg.doc_analyzer_backend.dto.DocumentResponse;
import org.springframework.web.multipart.MultipartFile;

public interface DocumentAiService {
    DocumentResponse mapToHtml(MultipartFile file);
}
