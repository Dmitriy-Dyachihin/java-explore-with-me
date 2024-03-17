package ru.practicum.ewm.controllers.pub;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.client.StatClient;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.ewm.dtos.event.EventFullDto;
import ru.practicum.ewm.dtos.event.EventShortDto;
import ru.practicum.ewm.enums.SortBy;
import ru.practicum.ewm.services.EventService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Validated
public class PublicEventController {

    private final EventService eventService;

    private final RestTemplateBuilder restTemplateBuilder;

    @Value("${stat-server.url}")
    private String statServerUrl;

    @GetMapping
    public List<EventShortDto> getEventsByUser(@RequestParam(name = "text", required = false) String text,
                                               @RequestParam(name = "categories", required = false) List<Long> categories,
                                               @RequestParam(name = "paid", required = false) Boolean paid,
                                               @RequestParam(name = "rangeStart", required = false) String rangeStart,
                                               @RequestParam(name = "rangeEnd", required = false) String rangeEnd,
                                               @RequestParam(name = "onlyAvailable", required = false) boolean onlyAvailable,
                                               @RequestParam(name = "sort", required = false) SortBy sort,
                                               @RequestParam(name = "from", defaultValue = "0") @Min(0) Integer from,
                                               @RequestParam(name = "size", defaultValue = "10") @Min(1) Integer size,
                                               HttpServletRequest request) {
        addHit(request);
        return eventService.getEventsByUserWithParams(text, categories, paid, rangeStart, rangeEnd, onlyAvailable,
                sort, from, size, request);
    }

    @GetMapping("/{id}")
    public EventFullDto getEventById(@PathVariable Long id, HttpServletRequest request) {
        addHit(request);
        return eventService.getEventById(id, request);
    }

    private void addHit(HttpServletRequest request) {
        StatClient statClient = new StatClient(statServerUrl, restTemplateBuilder);

        EndpointHitDto requestDto = new EndpointHitDto();
        requestDto.setTimestamp(LocalDateTime.now());
        requestDto.setUri(request.getRequestURI());
        requestDto.setApp("event-service");
        requestDto.setIp(request.getRemoteAddr());
        statClient.createEndpointHit(requestDto);
    }
}
