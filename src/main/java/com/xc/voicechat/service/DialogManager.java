package com.xc.voicechat.service;

import com.xc.voicechat.domain.entity.ChatMessage;
import com.xc.voicechat.domain.entity.SessionMessage;
import com.xc.voicechat.domain.enums.MessageType;
import com.xc.voicechat.processer.StreamResponseProcessor;
import com.xc.voicechat.tts.TTService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicReference;


@Slf4j
public class DialogManager {
    private final ChatClient chatClient;
    private final TTService ttService;
    private final AtomicReference<reactor.core.Disposable> currentDialogDisposable = new AtomicReference<>();

    public DialogManager(ChatClient chatClient, TTService ttService) {
        this.chatClient = chatClient;
        this.ttService = ttService;
    }


    public void processCompleteSentence(String text, WebSocketSession session) {
        StreamResponseProcessor processor = new StreamResponseProcessor(ttService, session);
        // 流式获取LLM回复
        Flux<String> content = chatClient.prompt(text)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, session.getId()))
                .stream().content();

        //流处理
        Disposable disposable = content.filter(StringUtils::hasText)
                .doOnNext(chunk -> {
                    try {
                        //断句tts
                        processor.processChunk(chunk);
                        // 文本消息推送
                        ChatMessage message = new ChatMessage(chunk, false);

                        session.sendMessage(new TextMessage(message.toJson()));
                    } catch (IOException e) {
                        log.error("大模型流式回复处理错误", e);
                    }
                })
                .doOnError(error -> log.error("大模型调用发生错误", error))
                .doOnComplete(() -> {
                            processor.flush();
                            try {
                                // 最后推送完成标记
                                ChatMessage completeMessage = new ChatMessage("", true);
                                session.sendMessage(new TextMessage(completeMessage.toJson()));
                                // 发送单次对话结束标识
                                session.sendMessage(new TextMessage(new SessionMessage(MessageType.DIALOG_END.getType()).toJson()));
                            } catch (IOException e) {
                                log.error("发送对话结束消息错误", e);
                            }

                        }

                ).doFinally(t -> processor.close()).subscribe();

        // 保存当前的对话流，用于打断
        currentDialogDisposable.set(disposable);
    }


    public void sendWelcome(String welcomeMessage, WebSocketSession session) {
        if (welcomeMessage != null && !welcomeMessage.trim().isEmpty()) {
            try {
                ByteBuffer audio = ttService.streamTTS(welcomeMessage);
                if (audio != null) {
                    // 音频推送
                    session.sendMessage(new BinaryMessage(audio));
                }

                ChatMessage completeMessage = new ChatMessage(welcomeMessage, true);
                session.sendMessage(new TextMessage(completeMessage.toJson()));
            } catch (IOException e) {
                log.error("发送音频片段出错", e);
            }
        }
    }

    public void interruptAI() {
        log.info("打断AI对话");
        reactor.core.Disposable disposable = currentDialogDisposable.get();
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
            currentDialogDisposable.set(null);
        }
    }
}