package com.lianpayhub.web.admin;

import javax.validation.constraints.NotBlank;

public class SendEmailRequest {
    private Long configId;
    @NotBlank
    private String to;
    @NotBlank
    private String subject;
    @NotBlank
    private String content;
    private Boolean html;

    public Long configId() { return configId; }
    public String to() { return to; }
    public String subject() { return subject; }
    public String content() { return content; }
    public Boolean html() { return html; }
}
