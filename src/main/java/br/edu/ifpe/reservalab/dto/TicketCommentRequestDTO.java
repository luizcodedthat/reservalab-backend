package br.edu.ifpe.reservalab.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** O autor do comentário é sempre o usuário autenticado. */
@Data
public class TicketCommentRequestDTO {

    @NotBlank(message = "Conteúdo é obrigatório")
    @Size(max = 2000, message = "O comentário deve ter no máximo 2000 caracteres")
    private String content;
}
