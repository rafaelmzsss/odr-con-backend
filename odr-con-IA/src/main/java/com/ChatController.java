package com.example.demo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ResponseEntity<ChatService.TriagemResponse> conversar(@RequestBody ChatRequest request) {
        if (request == null || request.message() == null || request.message().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new ChatService.TriagemResponse(
                            request != null ? request.sessionId() : null,
                            "Por favor, descreva o ocorrido ou sua dúvida para iniciarmos o acolhimento.",
                            "INDEFINIDO",
                            "Aguardando Relato",
                            false,
                            false
                    ));
        }

        ChatService.TriagemResponse resultado = chatService.processarMensagem(
                request.sessionId(),
                request.message()
        );
        return ResponseEntity.ok(resultado);
    }

    public record ChatRequest(String sessionId, String message) {}
}