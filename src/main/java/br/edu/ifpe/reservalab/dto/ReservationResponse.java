package br.edu.ifpe.reservalab.dto;

import br.edu.ifpe.reservalab.model.Reservation;
import br.edu.ifpe.reservalab.model.ReservationTimeBlock;
import br.edu.ifpe.reservalab.enums.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public record ReservationResponse(
        Long id,
        Long laboratoryId,
        String laboratoryName,
        Long requestedByUserId,
        String requestedByName,
        Long approvedByUserId,
        String approvedByName,
        Long groupId,
        LocalDate reservationDate,
        String purpose,
        ReservationStatus status,
        String rejectionReason,
        String notes,
        Integer totalDurationMinutes,
        Integer occurrenceNumber,
        List<TimeBlockResponse> timeBlocks,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public record TimeBlockResponse(
            Long id,
            LocalTime startTime,
            LocalTime endTime,
            Integer blockOrder,
            Integer durationMinutes
    ) {
        public static TimeBlockResponse from(ReservationTimeBlock block) {
            return new TimeBlockResponse(
                    block.getId(),
                    block.getStartTime(),
                    block.getEndTime(),
                    block.getBlockOrder(),
                    block.getDurationMinutes()
            );
        }
    }

    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getLaboratory().getId(),
                reservation.getLaboratory().getName(),
                reservation.getRequestedBy().getId(),
                reservation.getRequestedBy().getName(),
                reservation.getApprovedBy() != null ? reservation.getApprovedBy().getId()   : null,
                reservation.getApprovedBy() != null ? reservation.getApprovedBy().getName() : null,
                reservation.getGroup()      != null ? reservation.getGroup().getId()         : null,
                reservation.getReservationDate(),
                reservation.getPurpose(),
                reservation.getStatus(),
                reservation.getRejectionReason(),
                reservation.getNotes(),
                reservation.getTotalDurationMinutes(),
                reservation.getOccurrenceNumber(),
                reservation.getTimeBlocks().stream()
                        .map(TimeBlockResponse::from)
                        .toList(),
                reservation.getCreatedAt(),
                reservation.getUpdatedAt()
        );
    }
}