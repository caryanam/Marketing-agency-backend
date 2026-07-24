package com.marketingagencybackend.service;

import com.marketingagencybackend.dto.WhatsAppTemplateDTO;
import java.util.List;

public interface WhatsAppTemplateService {
    List<WhatsAppTemplateDTO> getAllTemplates();
    WhatsAppTemplateDTO createTemplate(WhatsAppTemplateDTO request);
}
