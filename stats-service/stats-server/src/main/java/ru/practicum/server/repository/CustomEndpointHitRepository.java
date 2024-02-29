package ru.practicum.server.repository;

import ru.practicum.server.model.Stats;

import java.time.LocalDateTime;
import java.util.List;

public interface CustomEndpointHitRepository {

    List<Stats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique);
}
