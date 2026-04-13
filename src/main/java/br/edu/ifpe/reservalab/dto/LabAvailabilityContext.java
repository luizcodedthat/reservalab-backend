package br.edu.ifpe.reservalab.dto;

import java.util.List;

public record LabAvailabilityContext(
        Long id,
        String name,
        int capacity,
        int computerCount
) {}