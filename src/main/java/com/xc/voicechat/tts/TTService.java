package com.xc.voicechat.tts;

import com.alibaba.dashscope.audio.ttsv2.SpeechSynthesisAudioFormat;
import com.alibaba.dashscope.audio.ttsv2.SpeechSynthesisParam;
import com.alibaba.dashscope.audio.ttsv2.SpeechSynthesizer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;

@Slf4j
public class TTService {

    private final SpeechSynthesizer synthesizer;

    private static final String MODEL = "cosyvoice-v2";
    private static final String VOICE = "longwan_v2";

    public TTService(String apiKey) {
        SpeechSynthesisParam synthesisParam = buildSpeechSynthesisParam(apiKey);
        this.synthesizer = new SpeechSynthesizer(synthesisParam, null);
    }


    @SneakyThrows
    public ByteBuffer streamTTS(String sentence) {
        if (sentence == null || sentence.trim().isEmpty()) {
            log.warn("输入空文本，语音生成跳过");
            return null;
        }
        ByteBuffer audio = synthesizer.call(sentence);
        if (audio == null) {
            log.warn("语音生成返回空字节，sentence: [{}]", sentence);
            return null;
        }
        return audio;
    }


    private SpeechSynthesisParam buildSpeechSynthesisParam(String apiKey) {
        return SpeechSynthesisParam.builder()
                .apiKey(apiKey)
                .model(MODEL)
                .voice(VOICE)
                .format(SpeechSynthesisAudioFormat.PCM_16000HZ_MONO_16BIT)
                .build();
    }
}
