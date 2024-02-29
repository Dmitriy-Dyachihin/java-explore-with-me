package ru.practicum.server.mapper;

import org.mapstruct.Mapper;
import ru.practicum.dto.StatsDto;
import ru.practicum.server.model.Stats;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StatsMapper {

    List<StatsDto> toListOfStatsDto(List<Stats> stats);

    StatsDto toStatsDto(Stats stats);
}
