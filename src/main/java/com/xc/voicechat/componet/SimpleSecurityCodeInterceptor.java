package com.xc.voicechat.componet;

import com.xc.voicechat.config.VoiceChatConfig;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.HandshakeFailureException;

import java.util.Map;

@Slf4j
@Component
public class SimpleSecurityCodeInterceptor implements HandshakeInterceptor {

    @Resource
    private VoiceChatConfig voiceChatConfig;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse
            response, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        ServletServerHttpRequest serverHttpRequest = (ServletServerHttpRequest) request;
        String security = serverHttpRequest.getServletRequest().getParameter("security");
        if (voiceChatConfig.isEnableAuth()) {
            if (!voiceChatConfig.auth(security)) {
                log.error("安全码校验不通过...security = {}", security);
                return false;
            }
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse
            response, WebSocketHandler wsHandler, Exception exception) {
        log.info("安全码认证通过");
    }
}
