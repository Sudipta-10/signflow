package com.example.DocsSignatureAppBE.Service;

import com.example.DocsSignatureAppBE.Util.PdfUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureOptions;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Calendar;

@Service
public class PdfSignatureService {

    private final PdfUtils pdfUtils;

    @Value("${signature.keystore.path:keystore.p12}")
    private String keystorePath;

    @Value("${signature.keystore.password:changeit}")
    private String keystorePassword;

    @Value("${signature.key.alias:mykey}")
    private String keyAlias;

    @Value("${signature.output.dir:./signed-uploads}")
    private String outputDir;

    public PdfSignatureService(PdfUtils pdfUtils) {
        this.pdfUtils = pdfUtils;
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * Embed multiple images at percentage coordinates on the pages and save as a new PDF
     */
    public File embedImages(String inputPdfPath, String imagePath, java.util.List<com.example.DocsSignatureAppBE.DTO.SignaturePositionDto> positions) throws IOException {
        if (imagePath == null || imagePath.isEmpty() || !new File(imagePath).exists() || positions == null || positions.isEmpty()) {
            return new File(inputPdfPath);
        }

        PDDocument document = pdfUtils.load(inputPdfPath);
        try {
            PDImageXObject pdImage = PDImageXObject.createFromFile(imagePath, document);

            for (com.example.DocsSignatureAppBE.DTO.SignaturePositionDto pos : positions) {
                int pageIndex = Math.max(0, pos.getPageNumber() - 1);
                // Ensure page index is valid
                if (pageIndex >= document.getNumberOfPages()) continue;

                PDPage page = document.getPage(pageIndex);
                PDRectangle media = page.getMediaBox();

                float x = (pos.getXPercent() / 100f) * media.getWidth();
                // PDFBox origin is Bottom-Left, but Frontend origin is Top-Left. Invert Y.
                float y = media.getHeight() - ((pos.getYPercent() / 100f) * media.getHeight()) - pos.getHeight();

                try (PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
                    contentStream.drawImage(pdImage, x, y, pos.getWidth(), pos.getHeight());
                }
            }

            pdfUtils.ensureDirectory(outputDir);
            String outPath = Paths.get(outputDir, "stamped_" + Paths.get(inputPdfPath).getFileName()).toString();
            document.save(outPath);
            return new File(outPath);
        } finally {
            document.close();
        }
    }

    /**
     * Embed an image at percentage coordinates on the page and save as a new PDF
     */
    public File embedImage(String inputPdfPath, String imagePath, int pageNumber, float xPercent, float yPercent, float widthPx, float heightPx) throws IOException {
        if (imagePath == null || imagePath.isEmpty() || !new File(imagePath).exists()) {
            // Skip stamping if image doesn't exist; just return the original file to be signed
            return new File(inputPdfPath);
        }

        PDDocument document = pdfUtils.load(inputPdfPath);
        try {
            int pageIndex = Math.max(0, pageNumber - 1);
            PDPage page = document.getPage(pageIndex);

            PDImageXObject pdImage = PDImageXObject.createFromFile(imagePath, document);
            PDRectangle media = page.getMediaBox();

            float x = (xPercent / 100f) * media.getWidth();
            // PDFBox origin is Bottom-Left, but Frontend origin is Top-Left. Invert Y.
            float y = media.getHeight() - ((yPercent / 100f) * media.getHeight()) - heightPx;

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
                contentStream.drawImage(pdImage, x, y, widthPx, heightPx);
            }

            pdfUtils.ensureDirectory(outputDir);
            String outPath = Paths.get(outputDir, "stamped_" + Paths.get(inputPdfPath).getFileName()).toString();
            document.save(outPath);
            return new File(outPath);
        } finally {
            document.close();
        }
    }

    /**
     * Simple PKCS12 signing (detached CMS) using PDFBox CreateSignature approach
     * This is a simplified approach and uses PDFBox's internal signing flow.
     */
    public File signPdf(String inputPdfPath, String signerName, String reason, String location) throws Exception {
        // Load keystore
        KeyStore keystore = KeyStore.getInstance("PKCS12");
        try (var is = Files.newInputStream(Paths.get(keystorePath))) {
            keystore.load(is, keystorePassword.toCharArray());
        }

        PrivateKey privateKey = (PrivateKey) keystore.getKey(keyAlias, keystorePassword.toCharArray());
        Certificate[] certificateChain = keystore.getCertificateChain(keyAlias);

        // For simplicity we will use PDFBox's external signing flow to create a signed PDF
        // Create signature appearance is skipped - this will produce a cryptographic signature without visible appearance

        PDDocument document = Loader.loadPDF(new File(inputPdfPath));
        try {
            // Create PDSignature and set fields
            PDSignature signature = new PDSignature();
            signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
            signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
            signature.setName(signerName);
            signature.setLocation(location);
            signature.setReason(reason);
            signature.setSignDate(Calendar.getInstance());

            SignatureInterfaceImpl signing = new SignatureInterfaceImpl(privateKey, certificateChain);

            pdfUtils.ensureDirectory(outputDir);
            String signedPath = Paths.get(outputDir, "signed_" + Paths.get(inputPdfPath).getFileName()).toString();
            try (FileOutputStream fos = new FileOutputStream(signedPath)) {
                SignatureOptions options = new SignatureOptions();
                options.setPreferredSignatureSize(32768);
                document.addSignature(signature, signing, options);
                document.saveIncremental(fos);
            }

            return new File(signedPath);
        } finally {
            document.close();
        }
    }
}

