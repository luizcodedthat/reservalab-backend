package br.edu.ifpe.reservalab.service;

import br.edu.ifpe.reservalab.model.Laboratory;
import br.edu.ifpe.reservalab.repository.LaboratoryRepository;
import br.edu.ifpe.reservalab.dto.ReservationFilter;
import br.edu.ifpe.reservalab.dto.ReservationRequest;
import br.edu.ifpe.reservalab.dto.ReservationResponse;
import br.edu.ifpe.reservalab.dto.ReservationUpdateRequest;
import br.edu.ifpe.reservalab.model.Reservation;
import br.edu.ifpe.reservalab.model.ReservationTimeBlock;
import br.edu.ifpe.reservalab.enums.ReservationStatus;
import br.edu.ifpe.reservalab.exception.ConflictingReservationException;
import br.edu.ifpe.reservalab.exception.ConflictingReservationException.ConflictDetail;
import br.edu.ifpe.reservalab.repository.ReservationRepository;
import br.edu.ifpe.reservalab.specification.ReservationSpecification;
import br.edu.ifpe.reservalab.model.User;
import br.edu.ifpe.reservalab.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

        private static final List<ReservationStatus> CANCELLABLE_STATUSES = List.of(ReservationStatus.PENDING,
                        ReservationStatus.APPROVED);

        private static final List<ReservationStatus> IGNORED_ON_CONFLICT = List.of(ReservationStatus.CANCELLED,
                        ReservationStatus.REJECTED);

        private final ReservationRepository reservationRepository;
        private final LaboratoryRepository laboratoryRepository;
        private final UserRepository userRepository;

        @Override
        @Transactional
        public ReservationResponse create(ReservationRequest request) {
                Laboratory laboratory = laboratoryRepository.findById(request.laboratoryId())
                                .orElseThrow(() -> new EntityNotFoundException(
                                                "Laboratório não encontrado: id=" + request.laboratoryId()));

                if (!laboratory.isActive()) {
                        throw new IllegalStateException(
                                        "Laboratório inativo: " + laboratory.getCode());
                }

                User requestedBy = userRepository.findById(request.requestedByUserId())
                                .orElseThrow(() -> new EntityNotFoundException(
                                                "Usuário não encontrado: id=" + request.requestedByUserId()));

                if (!requestedBy.isActive()) {
                        throw new IllegalStateException(
                                        "Usuário inativo: " + requestedBy.getUsername());
                }

                validateNoConflicts(request);

                Reservation reservation = buildReservation(request, laboratory, requestedBy);
                Reservation saved = reservationRepository.save(reservation);

                log.info("Reserva criada: id={}, lab={}, data={}, usuario={}",
                                saved.getId(), laboratory.getCode(),
                                request.reservationDate(), requestedBy.getUsername());

                return ReservationResponse.from(saved);
        }

        @Override
        @Transactional(readOnly = true)
        public Page<ReservationResponse> findAll(Pageable pageable) {
                log.debug("Listando todas as reservas – page={}, size={}",
                                pageable.getPageNumber(), pageable.getPageSize());
                return reservationRepository.findAllFetched(pageable)
                                .map(ReservationResponse::from);
        }

        @Override
        @Transactional(readOnly = true)
        public Page<ReservationResponse> findAllByFilter(ReservationFilter filter, Pageable pageable) {
                log.debug("Listando reservas por filtro: {}", filter);
                Specification<Reservation> spec = ReservationSpecification.fromFilter(filter);
                return reservationRepository.findAll(spec, pageable)
                                .map(ReservationResponse::from);
        }

        @Override
        @Transactional(readOnly = true)
        public ReservationResponse findById(Long id) {
                return reservationRepository.findByIdFetched(id)
                                .map(ReservationResponse::from)
                                .orElseThrow(() -> new EntityNotFoundException("Reserva não encontrada: id=" + id));
        }

        @Override
        @Transactional
        public void cancel(Long id) {
                Reservation reservation = reservationRepository.findByIdFetched(id)
                                .orElseThrow(() -> new EntityNotFoundException("Reserva não encontrada: id=" + id));

                if (!reservation.isCancellable()) {
                        throw new IllegalStateException(
                                        "Não é possível cancelar reserva com status: " + reservation.getStatus());
                }

                reservation.setStatus(ReservationStatus.CANCELLED);
                reservationRepository.save(reservation);

                log.info("Reserva cancelada: id={}", id);
        }

        @Override
        @Transactional
        public ReservationResponse update(Long id, ReservationUpdateRequest request) {

                Reservation reservation = reservationRepository.findByIdFetched(id)
                                .orElseThrow(() -> new EntityNotFoundException("Reserva não encontrada: id=" + id));

                if (reservation.getStatus() != ReservationStatus.PENDING) {
                        throw new IllegalStateException(
                                        "Apenas reservas pendentes podem ser editadas. Status atual: "
                                                        + reservation.getStatus());
                }

                Laboratory laboratory = laboratoryRepository.findById(request.laboratoryId())
                                .orElseThrow(() -> new EntityNotFoundException(
                                                "Laboratório não encontrado: id=" + request.laboratoryId()));

                if (!laboratory.isActive()) {
                        throw new IllegalStateException("Laboratório inativo: " + laboratory.getCode());
                }

                validateNoConflictsForUpdate(id, request);

                reservation.setLaboratory(laboratory);
                reservation.setReservationDate(request.reservationDate());
                reservation.setPurpose(request.purpose());
                reservation.setNotes(request.notes());

                reservation.getTimeBlocks().clear();

                int totalDuration = 0;
                for (ReservationRequest.TimeBlockRequest b : request.timeBlocks()) {
                        ReservationTimeBlock block = buildTimeBlock(b, reservation);
                        reservation.getTimeBlocks().add(block);
                        totalDuration += b.durationMinutes();
                }
                reservation.setTotalDurationMinutes(totalDuration);

                Reservation saved = reservationRepository.save(reservation);

                log.info("Reserva atualizada: id={}, lab={}, data={}",
                                saved.getId(), laboratory.getCode(), request.reservationDate());

                return ReservationResponse.from(saved);
        }

        private void validateNoConflictsForUpdate(Long excludeId, ReservationUpdateRequest request) {
                List<ConflictDetail> conflicts = new ArrayList<>();

                for (ReservationRequest.TimeBlockRequest block : request.timeBlocks()) {
                        List<Long> conflictingIds = reservationRepository.findConflictingReservationIds(
                                        request.laboratoryId(),
                                        request.reservationDate(),
                                        block.startTime(),
                                        block.endTime(),
                                        IGNORED_ON_CONFLICT).stream()
                                        .filter(conflictId -> !conflictId.equals(excludeId))
                                        .toList();

                        if (!conflictingIds.isEmpty()) {
                                conflicts.add(new ConflictDetail(
                                                request.reservationDate(),
                                                block.startTime(),
                                                block.endTime(),
                                                null, null,
                                                conflictingIds.get(0)));
                        }
                }

                if (!conflicts.isEmpty()) {
                        throw new ConflictingReservationException(conflicts);
                }
        }

        @Override
        @Transactional
        public int cancelByFilter(ReservationFilter filter) {
                log.debug("Cancelando reservas por filtro: {}", filter);

                if (filter.groupId() != null) {
                        return cancelByGroupId(filter.groupId());
                }

                Specification<Reservation> spec = ReservationSpecification.fromFilter(filter)
                                .and(onlyCancellable());

                List<Long> ids = reservationRepository.findAll(spec)
                                .stream()
                                .map(Reservation::getId)
                                .toList();

                if (ids.isEmpty()) {
                        return 0;
                }

                int count = reservationRepository.bulkUpdateStatus(ids, ReservationStatus.CANCELLED);
                log.info("Cancelamento em lote: {} reservas canceladas pelo filtro={}", count, filter);
                return count;
        }

        private void validateNoConflicts(ReservationRequest request) {
                List<ConflictDetail> conflicts = new ArrayList<>();

                for (ReservationRequest.TimeBlockRequest block : request.timeBlocks()) {
                        List<Long> conflictingIds = reservationRepository.findConflictingReservationIds(
                                        request.laboratoryId(),
                                        request.reservationDate(),
                                        block.startTime(),
                                        block.endTime(),
                                        IGNORED_ON_CONFLICT);

                        log.info("Verificando conflito: lab={} data={} inicio={} fim={} — IDs conflitantes: {}",
                                        request.laboratoryId(), request.reservationDate(),
                                        block.startTime(), block.endTime(), conflictingIds);

                        if (!conflictingIds.isEmpty()) {
                                conflicts.add(new ConflictDetail(
                                                request.reservationDate(),
                                                block.startTime(),
                                                block.endTime(),
                                                null, null,
                                                conflictingIds.get(0)));
                        }
                }

                if (!conflicts.isEmpty()) {
                        throw new ConflictingReservationException(conflicts);
                }
        }

        private Reservation buildReservation(ReservationRequest request,
                        Laboratory laboratory,
                        User requestedBy) {
                int totalDuration = request.timeBlocks().stream()
                                .mapToInt(ReservationRequest.TimeBlockRequest::durationMinutes)
                                .sum();

                Reservation reservation = Reservation.builder()
                                .laboratory(laboratory)
                                .requestedBy(requestedBy)
                                .reservationDate(request.reservationDate())
                                .purpose(request.purpose())
                                .notes(request.notes())
                                .status(ReservationStatus.PENDING)
                                .totalDurationMinutes(totalDuration)
                                .build();

                List<ReservationTimeBlock> blocks = request.timeBlocks().stream()
                                .map(b -> buildTimeBlock(b, reservation))
                                .toList();

                reservation.getTimeBlocks().addAll(blocks);

                return reservation;
        }

        private ReservationTimeBlock buildTimeBlock(ReservationRequest.TimeBlockRequest blockRequest,
                        Reservation reservation) {
                return ReservationTimeBlock.builder()
                                .reservation(reservation)
                                .startTime(blockRequest.startTime())
                                .endTime(blockRequest.endTime())
                                .blockOrder(blockRequest.blockOrder())
                                .durationMinutes(blockRequest.durationMinutes())
                                .createdAt(LocalDateTime.now())
                                .build();
        }

        private int cancelByGroupId(Long groupId) {
                List<Long> ids = reservationRepository.findCancellableIdsByGroupId(groupId, CANCELLABLE_STATUSES);

                if (ids.isEmpty()) {
                        return 0;
                }

                int count = reservationRepository.bulkUpdateStatus(ids, ReservationStatus.CANCELLED);
                log.info("Cancelamento em lote: {} reservas canceladas para groupId={}", count, groupId);
                return count;
        }

        private Specification<Reservation> onlyCancellable() {
                return (root, query, cb) -> root.get("status").in(CANCELLABLE_STATUSES);
        }
}