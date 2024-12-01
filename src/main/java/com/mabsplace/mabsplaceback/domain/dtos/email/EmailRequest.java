package com.mabsplace.mabsplaceback.domain.dtos.email;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
@Builder
public class EmailRequest {
    private String to;
    private List<String> cc;
    private List<String> bcc;
    private String fromEmail;
    private String subject;
    private String headerText;
    private String body;
    private String footerText;
    private String companyName;

    public EmailRequest(String to, List<String> cc, String subject, String headerText, String body, String companyName) {
        this.to = to;
        this.cc = cc;
        this.subject = subject;
        this.headerText = headerText;
        this.body = body;
        this.companyName = companyName;
    }

}