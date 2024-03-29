package ru.practicum.ewm.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.StatClient;
import ru.practicum.dto.StatsDto;
import ru.practicum.ewm.dtos.event.EventFullDto;
import ru.practicum.ewm.dtos.event.EventShortDto;
import ru.practicum.ewm.dtos.event.NewEventDto;
import ru.practicum.ewm.dtos.event.UpdateEventAdminRequest;
import ru.practicum.ewm.dtos.event.UpdateEventUserRequest;
import ru.practicum.ewm.enums.EventState;
import ru.practicum.ewm.enums.SortBy;
import ru.practicum.ewm.enums.StateActionForAdmin;
import ru.practicum.ewm.enums.StateActionForUser;
import ru.practicum.ewm.exceptions.EntityNotFoundException;
import ru.practicum.ewm.exceptions.UncorrectedParametersException;
import ru.practicum.ewm.exceptions.UncorrectedRequestException;
import ru.practicum.ewm.mappers.EventMapper;
import ru.practicum.ewm.mappers.LocationMapper;
import ru.practicum.ewm.models.Category;
import ru.practicum.ewm.models.Event;
import ru.practicum.ewm.models.User;
import ru.practicum.ewm.repositories.CategoryRepository;
import ru.practicum.ewm.repositories.EventRepository;
import ru.practicum.ewm.repositories.UserRepository;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventMapper eventMapper;
    private final CategoryRepository categoryRepository;
    private final EntityManager entityManager;
    private final LocationMapper locationMapper;
    private final String datePattern = "yyyy-MM-dd HH:mm:ss";
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(datePattern);
    private final RestTemplateBuilder restTemplateBuilder;

    @Value("${stat-server.url}")
    private String statServerUrl;


    @Transactional(readOnly = true)
    @Override
    public List<EventShortDto> getEvents(Long userId, Integer from, Integer size) {
        log.info("Получение списка событий добавленных пользователем с id={}", userId);
        User user = userRepository.findById(userId).orElseThrow(() ->
                new EntityNotFoundException("Пользователь с указанным id не существует"));
        Pageable pageable = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findAllByInitiatorId(userId, pageable);
        return eventMapper.convert(events);
    }

    @Override
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        log.info("Создание события пользователем с id={}", userId);
        log.info("Параметры: {}", newEventDto);
        User user = userRepository.findById(userId).orElseThrow(() ->
                new EntityNotFoundException("Пользователь с указанным id не существует"));
        Category category = categoryRepository.findById(newEventDto.getCategory()).orElseThrow(() ->
                new EntityNotFoundException("Категории с указанным id не существует"));
        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new UncorrectedRequestException("Ошибка времени создания события");
        }
        Event event = eventMapper.convert(newEventDto);
        event.setCategory(category);
        event.setInitiator(user);
        Event eventToSave = eventRepository.save(event);
        log.info("Создано событие с id={}", eventToSave.getId());
        return eventMapper.convert(eventToSave);
    }

    @Transactional(readOnly = true)
    @Override
    public EventFullDto getEventByUser(Long userId, Long eventId) {
        log.info("Получение информации о событии, добавленного пользователем с id={}", userId);
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId).orElseThrow(() ->
                new EntityNotFoundException("События с указанным id не существует"));
        return eventMapper.convert(event);
    }

    @Override
    public EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {
        log.info("Обновление события с id={}, пользователем с id={}", eventId, userId);
        log.info("Параметры: {}", updateEventUserRequest);
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId).orElseThrow(() ->
                new EntityNotFoundException("События с указанным id не существует"));
        if (event.getState().equals(EventState.PUBLISHED)) {
            throw new UncorrectedParametersException("Изменить можно только отмененные события или события в состоянии ожидания модерации");//Еще по-другому можно проверить
        }
        if (updateEventUserRequest.getEventDate() != null && updateEventUserRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new UncorrectedRequestException("Ошибка времени создания события");
        }
        if (updateEventUserRequest.getAnnotation() != null) {
            event.setAnnotation(updateEventUserRequest.getAnnotation());
        }
        if (updateEventUserRequest.getCategory() != null) {
            event.setCategory(categoryRepository.findById(updateEventUserRequest.getCategory())
                    .orElseThrow(() -> new EntityNotFoundException("Не существует категории с указанным id")));
        }
        if (updateEventUserRequest.getDescription() != null) {
            event.setDescription(updateEventUserRequest.getDescription());
        }
        if (updateEventUserRequest.getEventDate() != null) {
            event.setEventDate(updateEventUserRequest.getEventDate());
        }
        if (updateEventUserRequest.getLocation() != null) {
            event.setLocation(locationMapper.convert(updateEventUserRequest.getLocation()));
        }
        if (updateEventUserRequest.getPaid() != null) {
            event.setPaid(updateEventUserRequest.getPaid());
        }
        if (updateEventUserRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventUserRequest.getParticipantLimit());
        }
        if (updateEventUserRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateEventUserRequest.getRequestModeration());
        }
        if (updateEventUserRequest.getStateAction() != null) {
            if (updateEventUserRequest.getStateAction().equals(StateActionForUser.CANCEL_REVIEW)) {
                event.setState(EventState.CANCELED);
            } else {
                event.setState(EventState.PENDING);
            }
        }
        if (updateEventUserRequest.getTitle() != null) {
            event.setTitle(updateEventUserRequest.getTitle());
        }
        return eventMapper.convert(eventRepository.save(event));
    }

    @Transactional(readOnly = true)
    @Override
    public List<EventFullDto> getEventsByAdmin(List<Long> users, List<EventState> states, List<Long> categoriesId,
                                               String rangeStart, String rangeEnd, Integer from, Integer size) {
        log.info("Получение списка событий с параметрами администратором");
        log.info("Параметры: users {}, states {}, categoriesId {}, rangeStart {}, rangeEnd {}, from {}, size {}",
                users, states, categoriesId, rangeStart, rangeEnd, from, size);
        LocalDateTime start = rangeStart != null ? LocalDateTime.parse(rangeStart, dateTimeFormatter) : null;
        LocalDateTime end = rangeEnd != null ? LocalDateTime.parse(rangeEnd, dateTimeFormatter) : null;
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> query = criteriaBuilder.createQuery(Event.class);
        Root<Event> root = query.from(Event.class);
        Predicate criteria = criteriaBuilder.conjunction();
        if (users != null && !users.isEmpty() && users.size() > 0) {
            Predicate containsUsers = root.get("initiator").in(users);
            criteria = criteriaBuilder.and(criteria, containsUsers);
        }
        if (states != null && !states.isEmpty()) {
            Predicate containsStates = root.get("state").in(states);
            criteria = criteriaBuilder.and(criteria, containsStates);
        }
        if (categoriesId != null && !categoriesId.isEmpty() && categoriesId.size() != 0) {
            Predicate containsCategories = root.get("category").in(categoriesId);
            criteria = criteriaBuilder.and(criteria, containsCategories);
        }
        if (rangeStart != null) {
            Predicate startTime = criteriaBuilder.greaterThanOrEqualTo(root.get("eventDate").as(LocalDateTime.class), start);
            criteria = criteriaBuilder.and(criteria, startTime);
        }
        if (rangeEnd != null) {
            Predicate endTime = criteriaBuilder.lessThanOrEqualTo(root.get("eventDate").as(LocalDateTime.class), end);
            criteria = criteriaBuilder.and(criteria, endTime);
        }
        query.select(root).where(criteria);
        List<Event> events = entityManager.createQuery(query)
                .setFirstResult(from)
                .setMaxResults(size)
                .getResultList();
        if (events.isEmpty()) {
            return new ArrayList<>();
        }
        return eventMapper.convertToFull(events);
    }

    @Override
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateEventAdminRequest) {
        log.info("Обновление события с id={}", eventId);
        log.info("Параметры: {} ", updateEventAdminRequest);
        Event event = eventRepository.findById(eventId).orElseThrow(() ->
                new EntityNotFoundException("События с указанным id не существует"));

        if (event.getState() != null && event.getState().equals(EventState.PUBLISHED)) {
            throw new UncorrectedParametersException("Изменить можно только отмененные события или события в состоянии ожидания модерации");
        }

        if (updateEventAdminRequest.getEventDate() != null) {
            if (updateEventAdminRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(2)) || updateEventAdminRequest.getEventDate().isBefore(LocalDateTime.now())) {
                throw new UncorrectedRequestException("Дата и время на которые намечено событие не может быть раньше, чем через два часа от текущего момента");
            }
        }

        if (updateEventAdminRequest.getAnnotation() != null) {
            event.setAnnotation(updateEventAdminRequest.getAnnotation());
        }
        if (updateEventAdminRequest.getCategory() != null) {
            event.setCategory(categoryRepository.findById(updateEventAdminRequest.getCategory())
                    .orElseThrow(() -> new EntityNotFoundException("Не существует категории с указанным id")));
        }
        if (updateEventAdminRequest.getDescription() != null) {
            event.setDescription(updateEventAdminRequest.getDescription());
        }

        if (updateEventAdminRequest.getEventDate() != null) {
            event.setEventDate(updateEventAdminRequest.getEventDate());
        }

        if (updateEventAdminRequest.getLocation() != null) {
            event.setLocation(updateEventAdminRequest.getLocation());
        }
        if (updateEventAdminRequest.getPaid() != null) {
            event.setPaid(updateEventAdminRequest.getPaid());
        }
        if (updateEventAdminRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventAdminRequest.getParticipantLimit());
        }
        if (updateEventAdminRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateEventAdminRequest.getRequestModeration());
        }
        if (updateEventAdminRequest.getStateAction() != null) {
            if (!event.getState().equals(EventState.PENDING)) {
                throw new UncorrectedParametersException("Событие уже отменено");
            }
            if (updateEventAdminRequest.getStateAction().equals(StateActionForAdmin.PUBLISH_EVENT)) {
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            }
            if (updateEventAdminRequest.getStateAction().equals(StateActionForAdmin.REJECT_EVENT)) {
                event.setState(EventState.CANCELED);
            }
        }
        if (updateEventAdminRequest.getTitle() != null) {
            event.setTitle(updateEventAdminRequest.getTitle());
        }
        return eventMapper.convert(eventRepository.save(event));
    }

    @Transactional(readOnly = true)
    @Override
    public List<EventShortDto> getEventsByUserWithParams(String text, List<Long> categories, Boolean paid,
                                                         String rangeStart, String rangeEnd, boolean onlyAvailable,
                                                         SortBy sort, Integer from, Integer size,
                                                         HttpServletRequest request) {
        log.info("Получение списка событий с параметрами пользователем");
        LocalDateTime start = rangeStart != null ? LocalDateTime.parse(rangeStart, dateTimeFormatter) : null;
        LocalDateTime end = rangeEnd != null ? LocalDateTime.parse(rangeEnd, dateTimeFormatter) : null;
        if (start != null && end != null && start.isAfter(end)) {
            throw new UncorrectedRequestException("Начало не может быть после конца");
        }
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> query = criteriaBuilder.createQuery(Event.class);
        Root<Event> root = query.from(Event.class);
        Predicate criteria = criteriaBuilder.conjunction();
        if (text != null && !text.isEmpty()) {
            Predicate annotation = criteriaBuilder.like(criteriaBuilder.lower(root.get("annotation")),
                    "%" + text.toLowerCase() + "%");
            Predicate description = criteriaBuilder.like(criteriaBuilder.lower(root.get("description")),
                    "%" + text.toLowerCase() + "%");
            Predicate containsText = criteriaBuilder.or(annotation, description);
            criteria = criteriaBuilder.and(criteria, containsText);
        }
        if (categories != null && !categories.isEmpty() && categories.size() != 0) {
            Predicate containsCategories = root.get("category").in(categories);
            criteria = criteriaBuilder.and(containsCategories);
        }
        if (paid != null) {
            Predicate containsPaid;
            if (paid) {
                containsPaid = criteriaBuilder.isTrue(root.get("paid"));
            } else {
                containsPaid = criteriaBuilder.isFalse(root.get("paid"));
            }
            criteria = criteriaBuilder.and(criteria, containsPaid);
        }
        if (rangeStart != null) {
            Predicate startTime = criteriaBuilder.greaterThanOrEqualTo(root.get("eventDate").as(LocalDateTime.class), start);
            criteria = criteriaBuilder.and(criteria, startTime);
        }
        if (rangeEnd != null) {
            Predicate endTime = criteriaBuilder.lessThanOrEqualTo(root.get("eventDate").as(LocalDateTime.class), end);
            criteria = criteriaBuilder.and(criteria, endTime);
        }
        query.select(root).where(criteria).orderBy(criteriaBuilder.asc(root.get("eventDate")));
        List<Event> events = entityManager.createQuery(query)
                .setFirstResult(from)
                .setMaxResults(size)
                .getResultList();
        if (onlyAvailable) {
            events = events.stream()
                    .filter((e -> e.getParticipantLimit() > e.getConfirmedRequests()))
                    .collect(Collectors.toList());
        }
        if (sort != null) {
            if (sort.equals(SortBy.EVENT_DATE)) {
                events = events.stream()
                        .sorted(Comparator.comparing(Event::getEventDate))
                        .collect(Collectors.toList());
            } else {
                events = events.stream()
                        .sorted(Comparator.comparing(Event::getViews))
                        .collect(Collectors.toList());
            }
        }
        if (events.size() == 0) {
            return new ArrayList<>();
        }
        return eventMapper.convert(events);
    }

    @Transactional(readOnly = true)
    @Override
    public EventFullDto getEventById(Long id, HttpServletRequest request) {
        log.info("Получение события с id={}", id);
        Event event = eventRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException("Не существует события с указанным id"));
        if (event.getPublishedOn() == null || !event.getState().equals(EventState.PUBLISHED)) {
            throw new EntityNotFoundException("Событие не опубликовано");
        }
        List<Event> events = new ArrayList<>();
        events.add(event);
        StatClient statClient = new StatClient(statServerUrl, restTemplateBuilder);

        List<StatsDto> stats = statClient.getStats(LocalDateTime.now().minusYears(6), LocalDateTime.now(),
                List.of(request.getRequestURI()), true);
        if (!stats.isEmpty()) {
            Long hits = stats.get(0).getHits();
            setViews(id, hits);
        }
        return eventMapper.convert(event);
    }

    @Override
    @Transactional
    public void setViews(Long id, Long count) {
        Event currentEvent = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Не найдено событие по id = " + id));
        currentEvent.setViews(count);
        eventRepository.save(currentEvent);
    }
}
