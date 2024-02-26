package ru.practicum.server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.StatsDto;
import ru.practicum.server.exception.DateException;
import ru.practicum.server.mapper.EndpointHitMapper;
import ru.practicum.server.mapper.StatsMapper;
import ru.practicum.server.repository.EndpointHitRepository;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class StatsServiceImpl implements StatsService {

    private final EndpointHitMapper mapper;
    private final EndpointHitRepository repository;
    private final StatsMapper statsMapper;

    @Transactional
    @Override
    public EndpointHitDto createEndpointHit(EndpointHitDto endpointHitDto) {
        log.info("Создана сущность:{}", endpointHitDto.toString());
        EndpointHitDto response = mapper.toEndpointHitDto(repository.save(mapper.toEndpointHit(endpointHitDto)));
        return response;
    }

    @Transactional(readOnly = true)
    @Override
    public List<StatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        if (end.isBefore(start)) {
            throw new DateException("Конец не может быть после начала");
        }
        log.info("Get stats from:{} to:{}", start, end);
        return statsMapper.toListOfStatsDto(repository.getStats(start, end, uris, unique));
    }
}