package br.edu.ifpe.reservalab.controller;

import br.edu.ifpe.reservalab.dto.CommentVoteRequest;
import br.edu.ifpe.reservalab.model.User;
import br.edu.ifpe.reservalab.service.CommentVoteService;
import br.edu.ifpe.reservalab.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments/{commentId}/votes")
@RequiredArgsConstructor
public class CommentVoteController {

    private final CommentVoteService voteService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<Void> vote(
            @PathVariable Long commentId,
            @RequestBody @Valid CommentVoteRequest request,
            @AuthenticationPrincipal String email
    ) {
        User user = userService.getEntityByEmail(email);
        voteService.vote(commentId, user.getId(), request.type());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> removeVote(
            @PathVariable Long commentId,
            @AuthenticationPrincipal String email
    ) {
        User user = userService.getEntityByEmail(email);
        voteService.removeVote(commentId, user.getId());
        return ResponseEntity.noContent().build();
    }
}