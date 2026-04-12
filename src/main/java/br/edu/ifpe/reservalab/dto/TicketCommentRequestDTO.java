package br.edu.ifpe.reservalab.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TicketCommentRequestDTO {

    @NotNull(message = "ID do usuário é obrigatório")
    private Long userId;

    @NotBlank(message = "Conteúdo é obrigatório")
    private String content;
}