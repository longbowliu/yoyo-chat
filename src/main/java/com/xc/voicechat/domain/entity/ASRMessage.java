package com.xc.voicechat.domain.entity;

import com.xc.voicechat.domain.enums.MessageType;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = true)
public class ASRMessage extends ChatResponse {

    private String text;
    private Boolean isEnd;

    public ASRMessage(String text, Boolean isEnd) {
        super(MessageType.ASR.getType());
        this.text = text;
        this.isEnd = isEnd;
    }
}
