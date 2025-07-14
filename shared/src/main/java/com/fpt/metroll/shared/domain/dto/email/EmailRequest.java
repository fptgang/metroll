package com.fpt.metroll.shared.domain.dto.email;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {

    private String recipientEmail;
    private String recipientName;
    private String subject;
    private String templateName;
    private Map<String, Object> templateVariables;
    private boolean isHtml;

}