package com.enterprise.kb.ielts.dto;

import java.util.UUID;

public record AddLinkRequest(
        String targetType,
        UUID targetId,
        String note
) {}
