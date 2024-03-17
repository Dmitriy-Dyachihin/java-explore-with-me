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

    @Mapping(target = "confirmedRequests", constant = "0L")
    @Mapping(source = "category", target = "category.id")
    @Mapping(source = "location.lat", target = "location.lat")
    @Mapping(source = "location.lon", target = "location.lon")
    Event convert(NewEventDto newEventDto);

    EventFullDto convert(Event event);

    List<EventFullDto> convertToFull(List<Event> events);
}
