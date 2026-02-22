package com.example.DocsSignatureAppBE.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SignatureRequestSettingsDto {

    @JsonProperty("setOrder")
    private boolean setOrder;

    @JsonProperty("changeExpiration")
    private boolean changeExpiration;

    @JsonProperty("multipleRequests")
    private boolean multipleRequests;

    @JsonProperty("emailNotifications")
    private boolean emailNotifications;

    @JsonProperty("enableReminders")
    private boolean enableReminders;

    @JsonProperty("reminderDays")
    private int reminderDays;

    @JsonProperty("digitalSignature")
    private boolean digitalSignature;

    @JsonProperty("setLanguageState")
    private boolean setLanguageState;

    @JsonProperty("language")
    private String language;

    @JsonProperty("customizeEmail")
    private boolean customizeEmail;

    @JsonProperty("showUuid")
    private boolean showUuid;

    @JsonProperty("signatureVerificationCode")
    private boolean signatureVerificationCode;

    @JsonProperty("emailBranding")
    private boolean emailBranding;

    // Getters and Setters

    public boolean isSetOrder() { return setOrder; }
    public void setSetOrder(boolean setOrder) { this.setOrder = setOrder; }

    public boolean isChangeExpiration() { return changeExpiration; }
    public void setChangeExpiration(boolean changeExpiration) { this.changeExpiration = changeExpiration; }

    public boolean isMultipleRequests() { return multipleRequests; }
    public void setMultipleRequests(boolean multipleRequests) { this.multipleRequests = multipleRequests; }

    public boolean isEmailNotifications() { return emailNotifications; }
    public void setEmailNotifications(boolean emailNotifications) { this.emailNotifications = emailNotifications; }

    public boolean isEnableReminders() { return enableReminders; }
    public void setEnableReminders(boolean enableReminders) { this.enableReminders = enableReminders; }

    public int getReminderDays() { return reminderDays; }
    public void setReminderDays(int reminderDays) { this.reminderDays = reminderDays; }

    public boolean isDigitalSignature() { return digitalSignature; }
    public void setDigitalSignature(boolean digitalSignature) { this.digitalSignature = digitalSignature; }

    public boolean isSetLanguageState() { return setLanguageState; }
    public void setSetLanguageState(boolean setLanguageState) { this.setLanguageState = setLanguageState; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public boolean isCustomizeEmail() { return customizeEmail; }
    public void setCustomizeEmail(boolean customizeEmail) { this.customizeEmail = customizeEmail; }

    public boolean isShowUuid() { return showUuid; }
    public void setShowUuid(boolean showUuid) { this.showUuid = showUuid; }

    public boolean isSignatureVerificationCode() { return signatureVerificationCode; }
    public void setSignatureVerificationCode(boolean signatureVerificationCode) { this.signatureVerificationCode = signatureVerificationCode; }

    public boolean isEmailBranding() { return emailBranding; }
    public void setEmailBranding(boolean emailBranding) { this.emailBranding = emailBranding; }
}
