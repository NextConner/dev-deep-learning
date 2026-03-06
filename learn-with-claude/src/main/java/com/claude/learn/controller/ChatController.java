package com.claude.learn.controller;

import com.claude.learn.agent.PolicyAgent;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/api")
public class ChatController {


    private final PolicyAgent policyAgent;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    public ChatController(PolicyAgent policyAgent) {
        this.policyAgent = policyAgent;
    }

    @PostMapping("/chat")
    public String chat(@RequestBody ChatRequest request) {
        return policyAgent.chat(request.message());
    }

    public record ChatRequest(String message) {}


    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestParam String message){

        SseEmitter emitter = new SseEmitter(60_000L);
        //
        executor.submit(()->{

            try{
                policyAgent.streamChat(message)
                        .onNext(token -> {
                            try{
                                emitter.send(token);
                            }catch (IOException e){
                                emitter.completeWithError(e);
                            }
                        })
                        .onComplete( response -> emitter.complete())
                .onError( error -> emitter.completeWithError(error))
                        .start();
            }catch (Exception e){
                emitter.completeWithError(e);
            }
        });
        return  emitter;
    }

}
