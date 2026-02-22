package com.example.DocsSignatureAppBE.Service;

import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;

// Minimal SignatureInterface implementation using BouncyCastle
public class SignatureInterfaceImpl implements SignatureInterface {
    final PrivateKey privateKey;
    final Certificate[] certificateChain;

    SignatureInterfaceImpl(PrivateKey privateKey, Certificate[] certificateChain) {
        this.privateKey = privateKey;
        this.certificateChain = certificateChain;
    }

    @Override
    public byte[] sign(InputStream content) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA256");
            byte[] buffer = content.readAllBytes();
            byte[] hash = md.digest(buffer);

            // Build CMS signed data using BouncyCastle (simplified)
            CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
            ContentSigner sha1Signer = new JcaContentSignerBuilder("SHA256withRSA").setProvider("BC").build(privateKey);
            DigestCalculatorProvider digProv = new JcaDigestCalculatorProviderBuilder().setProvider("BC").build();
            JcaCertStore certs = new JcaCertStore(Arrays.asList(certificateChain));
            gen.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(digProv).build(sha1Signer, (X509Certificate)certificateChain[0]));
            gen.addCertificates(certs);

            CMSTypedData msg = new CMSProcessableByteArray(buffer);
            CMSSignedData signed = gen.generate(msg, false);
            return signed.getEncoded();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

}
