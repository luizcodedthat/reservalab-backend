package br.edu.ifpe.reservalab.service;

import br.edu.ifpe.reservalab.dto.ReservationFilter;
import br.edu.ifpe.reservalab.dto.ReservationResponse;
import br.edu.ifpe.reservalab.model.Reservation;
import br.edu.ifpe.reservalab.enums.ReservationStatus;
import br.edu.ifpe.reservalab.repository.ReservationRepository;
import br.edu.ifpe.reservalab.specification.ReservationSpecification;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private static final List<ReservationStatus> CANCELLABLE_STATUSES =
            List.of(ReservationStatus.PENDING, ReservationStatus.APPROVED);

    private final ReservationRepository reservationRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ReservationResponse> findAll(Pageable pageable) {
        log.debug("Listing all reservations – page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        return reservationRepository.findAllFetched(pageable)
                .map(ReservationResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservationResponse> findAllByFilter(ReservationFilter filter, Pageable pageable) {
        log.debug("Listing reservations by filter: {}", filter);
        Specification<Reservation> spec = ReservationSpecification.fromFilter(filter);
        return reservationRepository.findAll(spec, pageable)
                .map(ReservationResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationResponse findById(Long id) {
        return reservationRepository.findByIdFetched(id)
                .map(ReservationResponse::from)
                .orElseThrow(() -> new EntityNotFoundException("Reservation not found: id=" + id));
    }

    @Override
    @Transactional
    public void cancel(Long id) {
        Reservation reservation = reservationRepository.findByIdFetched(id)
                .orElseThrow(() -> new EntityNotFoundException("Reservation not found: id=" + id));

        if (!reservation.isCancellable()) {
            throw new IllegalStateException(
                    "Cannot cancel reservation with status: " + reservation.getStatus());
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);

        log.info("Reservation cancelled: id={}", id);
    }

    @Override
    @Transactional
    public int cancelByFilter(ReservationFilter filter) {
        log.debug("Cancelling reservations by filter: {}", filter);

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
        log.info("Bulk cancellation: {} reservations cancelled by filter={}", count, filter);
        return count;
    }

    private int cancelByGroupId(Long groupId) {
        List<Long> ids = reservationRepository.findCancellableIdsByGroupId(groupId, CANCELLABLE_STATUSES);

        if (ids.isEmpty()) {
            return 0;
        }

        int count = reservationRepository.bulkUpdateStatus(ids, ReservationStatus.CANCELLED);
        log.info("Bulk cancellation: {} reservations cancelled for groupId={}", count, groupId);
        return count;
    }

    private Specification<Reservation> onlyCancellable() {
        return (root, query, cb) -> root.get("status").in(CANCELLABLE_STATUSES);
    }
}