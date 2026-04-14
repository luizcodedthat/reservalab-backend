package br.edu.ifpe.reservalab.dto;

public record LabAvailabilityContext(
        Long id,
        String name,
        int capacity,
        int computerCount
) {}