package com.dbtraining.reconx.service;

import com.dbtraining.reconx.dto.TradeResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Maintains browser SSE subscriptions and broadcasts newly created trades. */
@Service
public class TradeStreamService {

    private final List<SseEmitter> subscribers = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(0L);
        subscribers.add(emitter);

        Runnable remove = () -> subscribers.remove(emitter);
        emitter.onCompletion(remove);
        emitter.onTimeout(remove);
        emitter.onError(error -> remove.run());

        try {
            emitter.send(SseEmitter.event()
                    .comment("connected")
                    .reconnectTime(3_000L));
        } catch (IOException exception) {
            remove.run();
            emitter.completeWithError(exception);
        }

        return emitter;
    }

    public void publish(TradeResponse trade) {
        for (SseEmitter emitter : subscribers) {
            try {
                // An unnamed event is delivered through EventSource.onmessage.
                emitter.send(SseEmitter.event()
                        .data(trade, MediaType.APPLICATION_JSON));
            } catch (IOException | IllegalStateException exception) {
                subscribers.remove(emitter);
                emitter.complete();
            }
        }
    }
}
