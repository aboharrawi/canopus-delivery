package com.canopus.delivery.stream.config;

import com.canopus.delivery.stream.util.StreamUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Component
public class MediaStreamBinaryWebSocketHandler implements WebSocketHandler {

    private final Map<String, Long> map = new ConcurrentHashMap<>();

    private static final String saveLocation = "/home/alpha1/testMedias";

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        Path path = Path.of(saveLocation, session.getId());
        return session.receive()
                //skip any non-binary "🙃" messages
                .filter(webSocketMessage -> webSocketMessage.getType().equals(WebSocketMessage.Type.BINARY))
                //possibly use a better streaming logic
                .flatMap(webSocketMessage -> StreamUtils.write(Mono.just(webSocketMessage.getPayload()), path.resolve(map.compute(session.getId(), (key, count) -> (count == null) ? 0 : count + 1) + ".webm"), null)).then();
    }
}