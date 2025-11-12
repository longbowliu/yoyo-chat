package com.xc.voicechat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class VoiceChatApplication {

    public static void main(String[] args) {
        SpringApplication.run(VoiceChatApplication.class, args).getEnvironment();
        colorPrint("语音对话助手启动成功");
    }



    private static void colorPrint(String text) {
        System.out.printf("\u001B[32m%s\u001B[0m%n", text);
    }


}
