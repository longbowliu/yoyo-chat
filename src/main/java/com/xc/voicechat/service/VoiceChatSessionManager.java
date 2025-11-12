package com.xc.voicechat.service;

import com.alibaba.dashscope.utils.JsonUtils;
import com.google.gson.JsonObject;
import com.xc.voicechat.config.VoiceChatConfig;
import com.xc.voicechat.domain.consts.VchatConst;
import com.xc.voicechat.domain.entity.InterruptAckMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.xc.voicechat.domain.consts.VchatConst.START;
import static com.xc.voicechat.domain.consts.VchatConst.STOP;
import static com.xc.voicechat.domain.enums.MessageType.INTERRUPT;

@Slf4j
public class VoiceChatSessionManager {
    private final Map<String, VoiceChatSession> sessions = new ConcurrentHashMap<>();
    private final VoiceChatConfig config;
    private final ChatClient chatClient;
    private final ChatMemory chatMemory;

    
    public VoiceChatSessionManager(VoiceChatConfig config, ChatClient chatClient, ChatMemory chatMemory) {
        this.config = config;
        this.chatClient = chatClient;
        this.chatMemory = chatMemory;
    }
    
    public void createSession(WebSocketSession session) {
        try (VoiceChatSession chatSession = sessions.computeIfAbsent(session.getId(),
                id -> new VoiceChatSession(config, chatClient, session, sessions.size()))) {

            log.info("新建对话session，session_id={}", chatSession.getSessionId());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public void destroySession(WebSocketSession session) {
        String sessionId = session.getId();
        chatMemory.clear(sessionId);
        VoiceChatSession voiceChatSession = sessions.remove(sessionId);
        if (voiceChatSession != null) {
            voiceChatSession.close();
        }
    }
    
    public void processAudioFrame(WebSocketSession session, byte[] audioData) {
        VoiceChatSession voiceChatSession = sessions.get(session.getId());
        if (voiceChatSession != null) {
            voiceChatSession.processAudioFrame(audioData);
        }
    }
    
    public void handleControlMessage(WebSocketSession session, String message) {
        VoiceChatSession voiceChatSession = sessions.get(session.getId());
        if (voiceChatSession != null) {
            try {
                // 尝试解析为JSON
                JsonObject jsonObject = JsonUtils.parse(message);
                String type = jsonObject.get(VchatConst.TYPE).getAsString();
                
                if (INTERRUPT.equal(type)) {
                    // 处理打断请求
                    voiceChatSession.interruptAI();
                    // 发送打断确认消息
                    session.sendMessage(new TextMessage(JsonUtils.toJson(new InterruptAckMessage())));
                } else if ("text".equals(type)) {
                    // 处理文本消息
                    String text = jsonObject.get("text").getAsString();
                    if (text != null && !text.trim().isEmpty()) {
                        // 发送单次对话开始标识
                        voiceChatSession.startASR();
                        // session.sendMessage(new TextMessage(new SessionMessage(MessageType.DIALOG_START.getType()).toJson()));
                        // 处理文本消息
                        voiceChatSession.processTextMessage(text, session);
                    }
                }
            } catch (Exception e) {
                if (START.equals(message)) {
                    voiceChatSession.startASR();
                } else if (STOP.equals(message)) {
                    voiceChatSession.stopASR();
                }
            }
        }
    }
}