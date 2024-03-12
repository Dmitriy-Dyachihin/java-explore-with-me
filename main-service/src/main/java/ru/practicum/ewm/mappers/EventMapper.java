package ru.practicum.ewm.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.ewm.dtos.event.EventFullDto;
import ru.practicum.ewm.dtos.event.EventShortDto;
import ru.practicum.ewm.dtos.event.NewEventDto;
import ru.practicum.ewm.models.Event;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EventMapper {

    List<EventShortDto> convert(List<Event> events);

    @Mapping(source = "category", target = "category.id")
    Event convert(NewEventDto newEventDto);

    EventFullDto convert(Event event);

    List<EventFullDto> convertToFull(List<Event> events);
}
