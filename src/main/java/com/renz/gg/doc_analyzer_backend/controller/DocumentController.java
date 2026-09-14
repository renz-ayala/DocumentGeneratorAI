package com.renz.gg.doc_analyzer_backend.controller;

import com.renz.gg.doc_analyzer_backend.dto.DocumentResponse;
import com.renz.gg.doc_analyzer_backend.dto.ExportRequest;
import com.renz.gg.doc_analyzer_backend.service.DocumentAiService;
import com.renz.gg.doc_analyzer_backend.util.DocumentGeneratorUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/document")
@RequiredArgsConstructor
public class DocumentController {
    private final DocumentAiService documentAiService;

    @PostMapping("/preview")
    public ResponseEntity<DocumentResponse> preview(@RequestParam("file")MultipartFile file) {
        var response = documentAiService.mapToHtml(file);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/export/pdf")
    public ResponseEntity<byte[]> exportToPdf(@RequestBody ExportRequest request) throws Exception {
        byte[] pdfBytes = DocumentGeneratorUtil.generatePdfFromHtml(request.htmlContent());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=document.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @PostMapping("/export/docx")
    public ResponseEntity<byte[]> exportToDocx(@RequestBody ExportRequest request) throws Exception {
        byte[] docxBytes = DocumentGeneratorUtil.generateDocxFromHtml(request.htmlContent());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=document.docx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                .body(docxBytes);
    }

}
