package com.example.DocsSignatureAppBE.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SignaturePositionDto {
    @JsonProperty("pageNumber")
    private Integer pageNumber;
    
    @JsonProperty("xPercent")
    private Float xPercent;
    
    @JsonProperty("yPercent")
    private Float yPercent;
    
    @JsonProperty("width")
    private Float width;
    
    @JsonProperty("height")
    private Float height;

    public Integer getPageNumber() { return pageNumber != null ? pageNumber : 1; }
    public void setPageNumber(Integer pageNumber) { this.pageNumber = pageNumber; }
    
    public Float getXPercent() { return xPercent != null ? xPercent : 0f; }
    public void setXPercent(Float xPercent) { this.xPercent = xPercent; }
    
    public Float getYPercent() { return yPercent != null ? yPercent : 0f; }
    public void setYPercent(Float yPercent) { this.yPercent = yPercent; }
    
    public Float getWidth() { return width != null ? width : 150f; }
    public void setWidth(Float width) { this.width = width; }
    
    public Float getHeight() { return height != null ? height : 50f; }
    public void setHeight(Float height) { this.height = height; }
}
