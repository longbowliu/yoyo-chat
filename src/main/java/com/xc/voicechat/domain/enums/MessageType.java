package com.xc.voicechat.domain.enums;

import lombok.Getter;

@Getter
public enum MessageType {
    ASR("asr"),
    CHAT("chat"),
    DIALOG_START("dialog_start"),
    DIALOG_END("dialog_end"),

    INTERRUPT("interrupt"),//from 前端
    INTERRUPT_ACK("interrupt_ack"),
    ;

    private final String type;

    MessageType(String type) {
        this.type = type;
    }

    public boolean equal(String type) {
        return this.type.equals(type);
    }
}