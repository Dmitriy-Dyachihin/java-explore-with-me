package ru.practicum.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;
import ru.practicum.server.model.EndpointHit;
import ru.practicum.server.model.Stats;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EndpointHitRepository extends JpaRepository<EndpointHit, Long>, QuerydslPredicateExecutor<EndpointHit>,
        CustomEndpointHitRepository {
    List<Stats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique);
}
