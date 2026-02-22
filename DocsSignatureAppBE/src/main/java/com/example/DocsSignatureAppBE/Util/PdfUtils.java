package com.example.DocsSignatureAppBE.Util;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class PdfUtils {

    public Float percentageToPixelX(float percentage, PDPage page) {
        PDRectangle media = page.getMediaBox();
        return (percentage / 100f) * media.getWidth();
    }

    public Float percentageToPixelY(float percentage, PDPage page) {
        PDRectangle media = page.getMediaBox();
        return (percentage / 100f) * media.getHeight();
    }

    public void ensureDirectory(String dir) throws IOException {
        Path path = Paths.get(dir);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
    }

    public PDDocument load(String path) throws IOException {
        return Loader.loadPDF(new File(path));
    }

}

