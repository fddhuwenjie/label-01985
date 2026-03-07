package com.sgs.websocket;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 消息处理器
 * 处理实时通知推送
 */
@Slf4j
@Component
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    private static final ConcurrentHashMap<String, WebSocketSession> SESSIONS = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        SESSIONS.put(sessionId, session);
        log.info("WebSocket 连接建立: sessionId={}, 当前在线数={}", sessionId, SESSIONS.size());

        sendMessage(session, "CONNECTED", "连接成功，欢迎使用学生成绩管理系统");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.info("收到消息: sessionId={}, message={}", session.getId(), payload);

        sendMessage(session, "PONG", "消息已收到: " + payload);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = session.getId();
        SESSIONS.remove(sessionId);
        log.info("WebSocket 连接关闭: sessionId={}, status={}, 当前在线数={}",
                sessionId, status, SESSIONS.size());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket 传输错误: sessionId={}", session.getId(), exception);
        if (session.isOpen()) {
            session.close(CloseStatus.SERVER_ERROR);
        }
        SESSIONS.remove(session.getId());
    }

    /**
     * 广播消息给所有在线用户
     */
    public void broadcastMessage(String type, String content) {
        Map<String, Object> messageData = createMessageData(type, content);
        String jsonMessage = JSONUtil.toJsonStr(messageData);

        log.info("广播消息: type={}, content={}, 接收人数={}", type, content, SESSIONS.size());

        SESSIONS.values().forEach(session -> {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(jsonMessage));
                } catch (IOException e) {
                    log.error("发送消息失败: sessionId={}", session.getId(), e);
                }
            }
        });
    }

    /**
     * 发送成绩更新通知
     */
    public void notifyScoreUpdate(Long studentId, String studentName, String courseName, Double score) {
        String content = String.format("学生 %s 的 %s 成绩已更新为 %.1f 分", studentName, courseName, score);
        broadcastMessage("SCORE_UPDATE", content);
    }

    /**
     * 发送成绩录入通知
     */
    public void notifyScoreCreated(String studentName, String courseName, Double score) {
        String content = String.format("新成绩录入: %s - %s: %.1f 分", studentName, courseName, score);
        broadcastMessage("SCORE_CREATED", content);
    }

    private void sendMessage(WebSocketSession session, String type, String content) throws IOException {
        Map<String, Object> messageData = createMessageData(type, content);
        session.sendMessage(new TextMessage(JSONUtil.toJsonStr(messageData)));
    }

    private Map<String, Object> createMessageData(String type, String content) {
        return Map.of(
                "type", type,
                "content", content,
                "timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                "onlineCount", SESSIONS.size()
        );
    }

    /**
     * 获取当前在线人数
     */
    public int getOnlineCount() {
        return SESSIONS.size();
    }
}
