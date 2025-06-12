package cn.websocket.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        String sessionId = session.getId();
        sessions.put(sessionId, session);

        // 发送自己的ID给客户端
        ObjectNode idMsg = mapper.createObjectNode();
        idMsg.put("type", "session-id");
        idMsg.put("id", sessionId);
        session.sendMessage(new TextMessage(mapper.writeValueAsString(idMsg)));

        // 如果有两个客户端连接，让它们互相连接
        if (sessions.size() == 2) {
            String[] ids = sessions.keySet().toArray(new String[0]);

            // 告诉第一个客户端连接第二个客户端
            ObjectNode userList1 = mapper.createObjectNode();
            userList1.put("type", "user-list");
            userList1.putArray("users").add(ids[1]);
            sessions.get(ids[0]).sendMessage(new TextMessage(mapper.writeValueAsString(userList1)));

            // 告诉第二个客户端连接第一个客户端
            ObjectNode userList2 = mapper.createObjectNode();
            userList2.put("type", "user-list");
            userList2.putArray("users").add(ids[0]);
            sessions.get(ids[1]).sendMessage(new TextMessage(mapper.writeValueAsString(userList2)));
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        String payload = message.getPayload();

        // Ping/Pong 处理
        if ("ping".equals(payload)) {
            session.sendMessage(new TextMessage("pong"));
            return;
        }

        // 解析消息并添加发送者信息
        ObjectNode node = mapper.readValue(payload, ObjectNode.class);
        node.put("from", session.getId());

        // 转发给目标客户端
        if (node.has("to")) {
            WebSocketSession target = sessions.get(node.get("to").asText());
            if (target != null && target.isOpen()) {
                target.sendMessage(new TextMessage(mapper.writeValueAsString(node)));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session.getId());
    }
}