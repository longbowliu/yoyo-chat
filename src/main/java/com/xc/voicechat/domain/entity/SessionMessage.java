package com.xc.voicechat.domain.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = true)
public class SessionMessage extends ChatResponse {
    private long timestamp;

    public SessionMessage(String type) {
        super(type);
        timestamp = System.currentTimeMillis();
    }
} 