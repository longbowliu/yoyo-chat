package com.xc.voicechat.domain.entity;

import com.xc.voicechat.domain.enums.MessageType;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = true)
public class InterruptAckMessage extends ChatResponse {

    public InterruptAckMessage() {
        super(MessageType.INTERRUPT_ACK.getType());
    }
}
