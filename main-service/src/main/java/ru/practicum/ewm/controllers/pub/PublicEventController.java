package ru.practicum.ewm.controllers.pub;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.dtos.event.EventFullDto;
import ru.practicum.ewm.dtos.event.EventShortDto;
import ru.practicum.ewm.enums.SortBy;
import ru.practicum.ewm.services.EventService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Min;
import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Validated
public class PublicEventController {

    private final EventService eventService;

    @GetMapping
    /*EventFullDto*/
    public List<EventShortDto> getEventsByUser(@RequestParam(name = "text", required = false) String text,
                                               @RequestParam(name = "categories", required = false) List<Long> categories,
                                               @RequestParam(name = "paid", required = false) Boolean paid,
                                               @RequestParam(name = "rangeStart", required = false) String rangeStart,
                                               @RequestParam(name = "rangeEnd", required = false) String rangeEnd,
                                               @RequestParam(name = "onlyAvailable", required = false) boolean onlyAvailable,
                                               @RequestParam(name = "sort", required = false) SortBy sort,
                                               @RequestParam(name = "from", required = false, defaultValue = "0") @Min(0) Integer from,
                                               @RequestParam(name = "size", required = false, defaultValue = "10") @Min(1) Integer size,
                                               HttpServletRequest request) {
        return eventService.getEventsByUserWithParams(text, categories, paid, rangeStart, rangeEnd, onlyAvailable,
                sort, from, size, request);
    }

    @GetMapping("/{id}")
    public EventFullDto getEventById(@PathVariable Long id, HttpServletRequest request) {
        return  eventService.getEventById(id, request);
    }
}
