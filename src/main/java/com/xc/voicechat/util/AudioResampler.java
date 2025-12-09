package com.xc.voicechat.util;

import lombok.extern.slf4j.Slf4j;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 音频采样率转换工具类
 * 用于将不同采样率的音频数据转换为16000Hz，以适配ASR服务
 */
@Slf4j
public class AudioResampler {
    
    private static final int TARGET_SAMPLE_RATE = 16000;
    private static final int TARGET_CHANNELS = 1;
    private static final int TARGET_BITS_PER_SAMPLE = 16;
    
    /**
     * 将音频数据重采样到16000Hz
     * @param audioData 原始音频数据
     * @param originalSampleRate 原始采样率
     * @return 重采样后的音频数据
     */
    public static byte[] resampleTo16kHz(byte[] audioData, int originalSampleRate) {
        if (originalSampleRate == TARGET_SAMPLE_RATE) {
            log.debug("音频已经是16kHz，无需转换");
            return audioData;
        }
        
        try {
            // 创建原始音频格式
            AudioFormat originalFormat = new AudioFormat(
                originalSampleRate,
                TARGET_BITS_PER_SAMPLE,
                TARGET_CHANNELS,
                true, // signed
                false // little-endian
            );
            
            // 创建目标音频格式
            AudioFormat targetFormat = new AudioFormat(
                TARGET_SAMPLE_RATE,
                TARGET_BITS_PER_SAMPLE,
                TARGET_CHANNELS,
                true, // signed
                false // little-endian
            );
            
            // 创建音频输入流
            ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
            AudioInputStream sourceStream = new AudioInputStream(bais, originalFormat, audioData.length / originalFormat.getFrameSize());
            
            // 进行采样率转换
            AudioInputStream convertedStream = AudioSystem.getAudioInputStream(targetFormat, sourceStream);
            
            // 读取转换后的数据
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = convertedStream.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            
            // 关闭流
            sourceStream.close();
            convertedStream.close();
            bais.close();
            baos.close();
            
            byte[] result = baos.toByteArray();
            log.info("音频重采样完成: {}Hz -> {}Hz, 数据长度: {} -> {}", 
                originalSampleRate, TARGET_SAMPLE_RATE, audioData.length, result.length);
            
            return result;
            
        } catch (IOException e) {
            log.error("音频重采样失败: {}", e.getMessage(), e);
            // 如果转换失败，返回原始数据
            return audioData;
        }
    }
    
    /**
     * 简单的线性插值重采样（适用于PCM 16位单声道）
     * 当Java Sound API不可用时的备用方案
     */
    public static byte[] linearResample(byte[] audioData, int originalSampleRate, int targetSampleRate) {
        if (originalSampleRate == targetSampleRate) {
            return audioData;
        }
        
        // 计算重采样比率
        double ratio = (double) targetSampleRate / originalSampleRate;
        int originalLength = audioData.length / 2; // 16位数据，每个样本2字节
        int targetLength = (int) (originalLength * ratio);
        
        short[] originalSamples = new short[originalLength];
        short[] targetSamples = new short[targetLength];
        
        // 转换字节数组为short数组
        for (int i = 0; i < originalLength; i++) {
            int lowByte = audioData[i * 2] & 0xFF;
            int highByte = audioData[i * 2 + 1] & 0xFF;
            originalSamples[i] = (short) (lowByte | (highByte << 8));
        }
        
        // 线性插值
        for (int i = 0; i < targetLength; i++) {
            double sourceIndex = i / ratio;
            int index1 = (int) Math.floor(sourceIndex);
            int index2 = Math.min(index1 + 1, originalLength - 1);
            double fraction = sourceIndex - index1;
            
            targetSamples[i] = (short) (originalSamples[index1] * (1 - fraction) + originalSamples[index2] * fraction);
        }
        
        // 转换short数组为字节数组
        byte[] result = new byte[targetLength * 2];
        for (int i = 0; i < targetLength; i++) {
            short sample = targetSamples[i];
            result[i * 2] = (byte) (sample & 0xFF);
            result[i * 2 + 1] = (byte) ((sample >> 8) & 0xFF);
        }
        
        return result;
    }
}