package br.edu.ifpe.reservalab.dto;

import br.edu.ifpe.reservalab.model.Computer;

public record ComputerResponse(
        Long id,
        String patrimonio,
        Long laboratoryId,
        String laboratoryName,
        String ip,
        String processador,
        String ram,
        String so,
        boolean active
) {
    public static ComputerResponse from(Computer c) {
        return new ComputerResponse(
                c.getId(),
                c.getPatrimonio(),
                c.getLaboratory().getId(),
                c.getLaboratory().getName(),
                c.getIp(),
                c.getProcessador(),
                c.getRam(),
                c.getSo(),
                c.isActive()
        );
    }
}