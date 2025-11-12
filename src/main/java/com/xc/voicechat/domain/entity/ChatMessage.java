package com.xc.voicechat.domain.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.xc.voicechat.domain.enums.MessageType;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatMessage extends ChatResponse {

    private String text;
    private Boolean isComplete;

    public ChatMessage(String text, Boolean isComplete) {
        super(MessageType.CHAT.getType());
        this.text = text;
        this.isComplete = isComplete;
    }

    public static ChatMessage error() {
        return new ChatMessage("不好意思，我临时有事，待会再聊吧。", true);
    }


}
