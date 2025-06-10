package cn.websocket.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SignalingHandler extends TextWebSocketHandler {

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    @Autowired
    ObjectMapper mapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String sessionId = session.getId();
        sessions.put(sessionId, session);
        System.out.println("Connected: " + sessionId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String sessionId = session.getId();
        String payload = message.getPayload();

        // 使用 Jackson 或其他 JSON 库来解析和修改消息
        ObjectNode node = (ObjectNode) mapper.readTree(payload);
        node.put("from", sessionId); // 为消息添加 'from' 字段

        String modifiedPayload = mapper.writeValueAsString(node);
        TextMessage broadcastMessage = new TextMessage(modifiedPayload);

        // 根据消息类型决定是广播还是点对点发送
        if (node.has("to")) {
            String toId = node.get("to").asText();
            WebSocketSession toSession = sessions.get(toId);
            if (toSession != null && toSession.isOpen()) {
                toSession.sendMessage(broadcastMessage);
            }
        } else {
            // 如果没有 'to' 字段，则广播给除自己外的所有人
            for (WebSocketSession s : sessions.values()) {
                if (s.isOpen() && !s.getId().equals(sessionId)) {
                    s.sendMessage(broadcastMessage);
                }
            }
        }
    }

    // 同时，当连接关闭时，也需要通知其他人
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = session.getId();
        sessions.remove(sessionId);
        System.out.println("Disconnected: " + sessionId);

        // 广播用户离开的消息
        String messagePayload = "{\"type\":\"user-left\", \"from\":\"" + sessionId + "\"}";
        TextMessage message = new TextMessage(messagePayload);

        for (WebSocketSession s : sessions.values()) {
            if (s.isOpen()) {
                s.sendMessage(message);
            }
        }
    }
}
