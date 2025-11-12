package com.xc.voicechat.controller;

import com.xc.voicechat.config.VoiceChatConfig;
import com.xc.voicechat.domain.consts.VchatConst;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")
public class ChatController {

    @Resource
    private VoiceChatConfig voiceChatConfig;

    /**
     * 安全码校验接口
     */
    @GetMapping("/checkSecurity")
    public ResponseEntity<String> checkSecurity(@RequestParam("code") String code) {
        boolean valid = voiceChatConfig.auth(code);
        return ResponseEntity.ok().body(valid ? VchatConst.OK : VchatConst.INVALID);
    }

}