package com.xc.voicechat.domain.entity;

import com.alibaba.dashscope.utils.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String type;

    public String toJson() {
        return JsonUtils.toJson(this);
    }
}
