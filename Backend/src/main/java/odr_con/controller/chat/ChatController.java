package odr_con.controller.chat;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import odr_con.service.chat.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
	private final ChatService chatService;

	public ChatController(ChatService chatService) { this.chatService = chatService; }

	@PostMapping
	public ResponseEntity<ChatService.TriagemResponse> conversar(@Valid @RequestBody ChatRequest request) {
		return ResponseEntity.ok(chatService.processarMensagem(request.sessionId(), request.message()));
	}

	public record ChatRequest(@Size(max = 80) String sessionId,
			@NotBlank @Size(max = 4000) String message) {}
}
