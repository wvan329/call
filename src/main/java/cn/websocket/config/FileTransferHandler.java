package cn.websocket.config;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@Component
public class FileTransferHandler extends TextWebSocketHandler {
    // 存储已连接的会话（最多2个）
    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        if (sessions.size() < 2) {
            sessions.add(session);
        } else {
            // 超过两人时可选择拒绝连接
            session.close(CloseStatus.SERVER_ERROR);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();

        // 如果是心跳包，直接回复 pong
        if ("ping".equals(payload)) {
            return;
        }
        // 收到一方的信令消息后转发给另一方
        for (WebSocketSession other : sessions) {
            if (!other.getId().equals(session.getId()) && other.isOpen()) {
                other.sendMessage(message);
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
    }
}
