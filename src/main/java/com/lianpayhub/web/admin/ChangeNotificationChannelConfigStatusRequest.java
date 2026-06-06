package com.lianpayhub.web.admin;

import com.lianpayhub.domain.notification.NotificationChannelStatus;
import javax.validation.constraints.NotNull;

public class ChangeNotificationChannelConfigStatusRequest {
    @NotNull
    private NotificationChannelStatus status;

    public NotificationChannelStatus status() { return status; }
}
