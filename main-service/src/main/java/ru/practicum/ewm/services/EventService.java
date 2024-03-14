package ru.practicum.ewm.services;

import ru.practicum.ewm.dtos.event.EventFullDto;
import ru.practicum.ewm.dtos.event.EventShortDto;
import ru.practicum.ewm.dtos.event.NewEventDto;
import ru.practicum.ewm.dtos.event.UpdateEventAdminRequest;
import ru.practicum.ewm.dtos.event.UpdateEventUserRequest;
import ru.practicum.ewm.enums.EventState;
import ru.practicum.ewm.enums.SortBy;
//import ru.practicum.ewm.models.Event;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface EventService {

    List<EventShortDto> getEvents(Long userId, Integer from, Integer size);

    EventFullDto createEvent(Long userId, NewEventDto newEventDto);

    EventFullDto getEventByUser(Long userId, Long eventId);

    EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest);

    List<EventFullDto> getEventsByAdmin(List<Long> users, List<EventState> states, List<Long> categoriesId,
                                        String rangeStart, String rangeEnd, Integer from, Integer size);

    EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateEventAdminRequest);

    List<EventShortDto> getEventsByUserWithParams(String text, List<Long> categories, Boolean paid, String rangeStart,
                                                  String rangeEnd, boolean onlyAvailable, SortBy sort, Integer from, Integer size,
                                                  HttpServletRequest request);

    EventFullDto getEventById(Long id, HttpServletRequest request);

    void setViews(Long id, Long count);
}
