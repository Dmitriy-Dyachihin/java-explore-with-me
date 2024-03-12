package ru.practicum.ewm.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.models.Request;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {

    List<Request> findAllByRequesterOrderById(Long id);

    Boolean existsByRequesterAndEvent(Long userId, Long eventId);

    List<Request> findAllByEvent(Long eventId);

    Optional<Request> findByRequesterAndId(Long userId, Long requestId);

    @Query("select r from Request as r join Event as e on r.event = e.id where r.event = :eventId  " +
            "and e.initiator.id = :userId")
    List<Request> findAllByEventWithInitiator(@Param("userId") Long userId, @Param("eventId") Long eventId);
}
