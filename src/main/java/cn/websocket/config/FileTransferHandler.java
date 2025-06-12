package cn.websocket.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class FileTransferHandler extends TextWebSocketHandler {

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    @Autowired
    ObjectMapper mapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String sessionId = session.getId();
        sessions.put(sessionId, session);
        System.out.println("Connected: " + sessionId);

        try {
            // 1. 发送自己的sessionId给新连接的客户端
            ObjectNode myIdMessage = mapper.createObjectNode();
            myIdMessage.put("type", "session-id");
            myIdMessage.put("id", sessionId);
            session.sendMessage(new TextMessage(mapper.writeValueAsString(myIdMessage)));

            // 2. 广播所有在线用户的列表（包括新加入的自己）
            ObjectNode userListMessage = mapper.createObjectNode();
            userListMessage.put("type", "user-list");
            ArrayNode idArray = mapper.createArrayNode();
            sessions.keySet().forEach(idArray::add); // 添加所有在线会话ID
            userListMessage.set("users", idArray);

            String userListPayload = mapper.writeValueAsString(userListMessage);
            for (WebSocketSession s : sessions.values()) {
                if (s.isOpen()) {
                    s.sendMessage(new TextMessage(userListPayload));
                }
            }

        } catch (IOException e) {
            System.err.println("Error sending initial messages: " + e.getMessage());
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String sessionId = session.getId();
        String payload = message.getPayload();

        // Ping/Pong 保持不变
        if ("ping".equals(payload)) {
            session.sendMessage(new TextMessage("pong"));
            return;
        }

        // 解析并修改消息（添加from字段）
        ObjectNode node = (ObjectNode) mapper.readTree(payload);
        node.put("from", sessionId); // 为消息添加 'from' 字段

        String modifiedPayload = mapper.writeValueAsString(node);
        TextMessage broadcastMessage = new TextMessage(modifiedPayload);

        // 根据消息类型决定是广播还是点对点发送
        if (node.has("to")) {
            String toId = node.get("to").asText();
            WebSocketSession toSession = sessions.get(toId);
            if (toSession != null && toSession.isOpen()) {
                System.out.println("Forwarding message from " + sessionId + " to " + toId + ": " + node.get("type"));
                toSession.sendMessage(broadcastMessage);
            } else {
                System.out.println("Target session " + toId + " not found or closed for message from " + sessionId);
                // 可以考虑向发送方发送一个错误消息
            }
        } else {
            // 如果没有 'to' 字段，则广播给除自己外的所有人（例如语音信令中的 offer/answer 广播，或用户列表更新）
            System.out.println("Broadcasting message from " + sessionId + ": " + node.get("type"));
            for (WebSocketSession s : sessions.values()) {
                // 如果是用户列表更新，需要发给自己，所以不加 !s.getId().equals(sessionId)
                // 但对于 WebRTC 信令 offer/answer 通常是点对点，不会广播
                // 这里保持原样，只发给其他人，如果是为了同步用户列表则需要特殊处理
                if (s.isOpen() && !s.getId().equals(sessionId)) { // 广播给除了自己以外的所有人
                    s.sendMessage(broadcastMessage);
                }
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = session.getId();
        sessions.remove(sessionId);
        System.out.println("Disconnected: " + sessionId + " (Status: " + status.getCode() + " - " + status.getReason() + ")");

        // 广播用户离开的消息
        ObjectNode userLeftMessage = mapper.createObjectNode();
        userLeftMessage.put("type", "user-left");
        userLeftMessage.put("from", sessionId);

        String messagePayload = mapper.writeValueAsString(userLeftMessage);
        TextMessage message = new TextMessage(messagePayload);

        for (WebSocketSession s : sessions.values()) {
            if (s.isOpen()) {
                s.sendMessage(message);
            }
        }
    }
}