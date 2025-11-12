package com.xc.voicechat.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Objects;


@Data
@Component
@ConfigurationProperties(prefix = "chat")
public class VoiceChatConfig {

    private String apiKey;

    private String welcomeMessage;

    private String security;

    private String knowledge;

    public static final String audioPath = System.getProperty("user.dir") + "/audio";


    public boolean isEnableAuth() {
        return StringUtils.hasText(security);
    }

    public boolean auth(String code) {
        return Objects.equals(security, code);
    }
}
