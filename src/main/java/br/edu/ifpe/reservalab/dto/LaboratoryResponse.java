package br.edu.ifpe.reservalab.dto;

import br.edu.ifpe.reservalab.model.Laboratory;

import java.time.LocalDateTime;

public record LaboratoryResponse(
        Long id,
        String name,
        String code,
        String description,
        Integer computerCount,
        Integer capacity,
        String building,
        String floor,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static LaboratoryResponse from(Laboratory laboratory) {
        return new LaboratoryResponse(
                laboratory.getId(),
                laboratory.getName(),
                laboratory.getCode(),
                laboratory.getDescription(),
                laboratory.getComputerCount(),
                laboratory.getCapacity(),
                laboratory.getBuilding(),
                laboratory.getFloor(),
                laboratory.isActive(),
                laboratory.getCreatedAt(),
                laboratory.getUpdatedAt()
        );
    }
}