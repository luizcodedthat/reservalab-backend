package br.edu.ifpe.reservalab.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LaboratoryCommentRequest(

        @NotNull(message = "O usuário é obrigatório")
        Long authorId,

        @NotBlank(message = "O conteúdo do comentário é obrigatório")
        @Size(max = 2000, message = "O comentário deve ter no máximo 2000 caracteres")
        String content

) {}