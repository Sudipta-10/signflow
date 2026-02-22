package com.example.DocsSignatureAppBE.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class FinalizeSignatureRequestDto {
    @JsonProperty("signatureBase64")
    private String signatureBase64;
    
    @JsonProperty("positions")
    private List<SignaturePositionDto> positions;

    public String getSignatureBase64() {
        return signatureBase64;
    }

    public void setSignatureBase64(String signatureBase64) {
        this.signatureBase64 = signatureBase64;
    }

    public List<SignaturePositionDto> getPositions() {
        return positions;
    }

    public void setPositions(List<SignaturePositionDto> positions) {
        this.positions = positions;
    }
}
