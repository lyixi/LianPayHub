package com.lianpayhub.service.notification;

import com.lianpayhub.domain.notification.NotificationChannelType;

public class NotificationSendResult {

    private final boolean success;
    private final NotificationChannelType channelType;
    private final String providerCode;
    private final String messageId;
    private final String message;

    public NotificationSendResult(boolean success, NotificationChannelType channelType, String providerCode,
                                  String messageId, String message) {
        this.success = success;
        this.channelType = channelType;
        this.providerCode = providerCode;
        this.messageId = messageId;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public NotificationChannelType getChannelType() {
        return channelType;
    }

    public String getProviderCode() {
        return providerCode;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getMessage() {
        return message;
    }
}
