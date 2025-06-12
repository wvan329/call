package cn.websocket.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private SignalingHandler signalingHandler;
    @Autowired
    private FileTransferHandler fileTransferHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(signalingHandler, "/ws/voice");
//                .setAllowedOrigins("*");

        registry.addHandler(fileTransferHandler, "/ws/file");
//                .setAllowedOrigins("*");
    }
}
