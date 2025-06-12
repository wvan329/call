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
    @Autowired ObjectMapper mapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        String id = session.getId();
        sessions.put(id, session);

        ObjectNode myId = mapper.createObjectNode();
        myId.put("type", "session-id");
        myId.put("id", id);
        session.sendMessage(new TextMessage(myId.toString()));

        ObjectNode userList = mapper.createObjectNode();
        userList.put("type", "user-list");
        ArrayNode users = mapper.createArrayNode();
        sessions.keySet().forEach(users::add);
        userList.set("users", users);

        String payload = userList.toString();
        for (WebSocketSession s : sessions.values()) {
            if (s.isOpen()) s.sendMessage(new TextMessage(payload));
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        ObjectNode msg = (ObjectNode) mapper.readTree(message.getPayload());
        msg.put("from", session.getId());

        String to = msg.has("to") ? msg.get("to").asText() : null;
        TextMessage out = new TextMessage(msg.toString());

        if (to != null && sessions.containsKey(to)) {
            sessions.get(to).sendMessage(out);
        } else {
            for (WebSocketSession s : sessions.values()) {
                if (!s.getId().equals(session.getId()) && s.isOpen()) {
                    s.sendMessage(out);
                }
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session.getId());
    }
}