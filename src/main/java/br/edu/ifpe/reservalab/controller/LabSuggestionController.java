package br.edu.ifpe.reservalab.controller;

import br.edu.ifpe.reservalab.dto.labSuggestion.LabSuggestionRequest;
import br.edu.ifpe.reservalab.dto.labSuggestion.LabSuggestionResponse;
import br.edu.ifpe.reservalab.service.LabSuggestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai/labs")
@RequiredArgsConstructor
public class LabSuggestionController {

    private final LabSuggestionService suggestionService;

    @PostMapping("/suggest")
    public ResponseEntity<LabSuggestionResponse> suggest(
            @RequestBody @Valid LabSuggestionRequest request
    ) {
        return ResponseEntity.ok(suggestionService.suggest(request));
    }
}