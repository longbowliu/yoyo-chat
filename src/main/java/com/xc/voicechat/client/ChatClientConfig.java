package com.xc.voicechat.client;

import com.xc.voicechat.config.WeatherProperties;
import com.xc.voicechat.tools.TimeTools;
import com.xc.voicechat.tools.WeatherTools;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;


@Configuration
@Slf4j
public class ChatClientConfig {

    private static String prompt = "你是一个博学的智能聊天助手，请根据用户提问回答！";


    @Resource
    private ChatMemory chatMemory;

    @Resource
    private WeatherProperties weatherProperties;


    @Bean
    ChatClient chatClient(ChatClient.Builder chatClientBuilder) {
        ClassPathResource resource = new ClassPathResource("static/prompt/system-prompt.txt");
        if (resource.exists()) {
            log.debug("读取到system-prompt.txt");
            try (InputStream inputStream = resource.getInputStream()) {
                byte[] bytes = FileCopyUtils.copyToByteArray(inputStream);
                prompt = new String(bytes, StandardCharsets.UTF_8);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }

        return chatClientBuilder
                .defaultSystem(prompt)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
//                .defaultAdvisors(new SimpleLoggerAdvisor())
//                .defaultOptions(ChatOptions.builder().topP(0.8).temperature(0.7d).build()) //这行配置会导致工具调用失效
                .defaultTools(new TimeTools(), new WeatherTools(weatherProperties))
                //设置云知识库
//                .defaultAdvisors(ragCloudAdvisor)
                .build();
    }

}