package com.xc.voicechat.processer;

import com.xc.voicechat.tts.TTService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

@Slf4j
public class StreamResponseProcessor {
    private final TTService ttService;
    private final WebSocketSession session;
    private final ExecutorService ttsExecutor;
    private final StringBuilder chatBuffer = new StringBuilder();

    // 标点符号和长度阈值
    private static final Pattern SENTENCE_END_PATTERN = Pattern.compile("[,.!?;:，。—！？；：）]");
    private static final int MAX_LENGTH_WITHOUT_PUNCTUATION = 30;

    public StreamResponseProcessor(TTService ttService, WebSocketSession session) {
        this.ttService = ttService;
        this.session = session;
        this.ttsExecutor = Executors.newSingleThreadExecutor();
    }

    public void processChunk(String chunk) {
        if (!StringUtils.hasText(chunk)) {
            return;
        }

        synchronized (chatBuffer) {
            chatBuffer.append(chunk);
            checkAndProcessBuffer();
        }
    }

    private void checkAndProcessBuffer() {
        String bufferStr = chatBuffer.toString();

        // 检查是否以标点符号结尾
        if (SENTENCE_END_PATTERN.matcher(bufferStr).find()) {
            int lastPunctuationIndex = findLastPunctuationIndex(bufferStr);
            if (lastPunctuationIndex != -1) {
                String completeSentence = bufferStr.substring(0, lastPunctuationIndex + 1);
                processSentenceAsync(completeSentence);
                chatBuffer.delete(0, lastPunctuationIndex + 1);
            }
        }
        // 检查长度是否超过阈值
        else if (bufferStr.length() >= MAX_LENGTH_WITHOUT_PUNCTUATION) {
            processSentenceAsync(bufferStr);
            chatBuffer.setLength(0);
        }
    }

    private void processSentenceAsync(String sentence) {
        ttsExecutor.submit(() -> {
            try {
                ByteBuffer audioData = ttService.streamTTS(sentence);
                if (session.isOpen()) {
                    session.sendMessage(new BinaryMessage(audioData));
                }
            } catch (Exception e) {
                log.error("发送句子音频出错", e);
            }
        });
    }



    public void close() {
        ttsExecutor.shutdown();
    }

    private int findLastPunctuationIndex(String str) {
        for (int i = str.length() - 1; i >= 0; i--) {
            char c = str.charAt(i);
            if (SENTENCE_END_PATTERN.matcher(String.valueOf(c)).matches()) {
                return i;
            }
        }
        return -1;
    }


    // 处理剩余内容（流结束时调用）
    public void flush() {
        synchronized (chatBuffer) {
            if (!chatBuffer.isEmpty()) {
                processSentenceAsync(chatBuffer.toString());
                chatBuffer.setLength(0);
            }
        }
    }
}