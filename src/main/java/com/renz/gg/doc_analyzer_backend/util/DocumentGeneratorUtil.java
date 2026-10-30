package com.renz.gg.doc_analyzer_backend.util;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.docx4j.convert.in.xhtml.XHTMLImporterImpl;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.ByteArrayOutputStream;

public class DocumentGeneratorUtil {

    public static byte[] generatePdfFromHtml(String htmlContent) throws Exception {
        var outputStream = new ByteArrayOutputStream();

        var builder = new PdfRendererBuilder();
        builder.useFastMode();
        builder.withHtmlContent(getXhtml(htmlContent), null);
        builder.toStream(outputStream);
        builder.run();

        return outputStream.toByteArray();
    }

    public static byte[] generateDocxFromHtml(String htmlContent) throws Exception {
        var wordPackage = WordprocessingMLPackage.createPackage();
        var importer = new XHTMLImporterImpl(wordPackage);
        var outputStream = new ByteArrayOutputStream();

        wordPackage.getMainDocumentPart().getContent().addAll(importer.convert(getXhtml(htmlContent), null));
        wordPackage.save(outputStream);

        return outputStream.toByteArray();
    }

    private static String getXhtml(String dirtyHtml) {
        Document doc = Jsoup.parse(dirtyHtml);
        doc.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
        return doc.html();
    }

}
