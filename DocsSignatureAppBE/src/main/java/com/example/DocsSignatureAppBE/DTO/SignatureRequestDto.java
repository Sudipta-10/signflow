package com.example.DocsSignatureAppBE.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class SignatureRequestDto {

    @JsonProperty("receivers")
    private List<ReceiverDto> receivers;

    @JsonProperty("settings")
    private SignatureRequestSettingsDto settings;

    public List<ReceiverDto> getReceivers() {
        return receivers;
    }

    public void setReceivers(List<ReceiverDto> receivers) {
        this.receivers = receivers;
    }

    public SignatureRequestSettingsDto getSettings() {
        return settings;
    }

    public void setSettings(SignatureRequestSettingsDto settings) {
        this.settings = settings;
    }
}
