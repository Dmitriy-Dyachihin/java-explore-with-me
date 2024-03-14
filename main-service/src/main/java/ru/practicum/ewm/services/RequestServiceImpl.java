package ru.practicum.ewm.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dtos.request.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.dtos.request.EventRequestStatusUpdateResult;
import ru.practicum.ewm.dtos.request.RequestDto;
import ru.practicum.ewm.enums.EventState;
import ru.practicum.ewm.enums.RequestStatus;
import ru.practicum.ewm.enums.RequestStatusToUpdate;
import ru.practicum.ewm.exceptions.EntityNotFoundException;
import ru.practicum.ewm.exceptions.UncorrectedParametersException;
import ru.practicum.ewm.mappers.RequestMapper;
import ru.practicum.ewm.models.Event;
import ru.practicum.ewm.models.Request;
import ru.practicum.ewm.models.User;
import ru.practicum.ewm.repositories.EventRepository;
import ru.practicum.ewm.repositories.RequestRepository;
import ru.practicum.ewm.repositories.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RequestServiceImpl implements RequestService {

    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final RequestMapper requestMapper;

    @Transactional(readOnly = true)
    @Override
    public List<RequestDto> getCurrentUserRequests(Long userId) {
        log.info("Получение списка заявок на события пользователя с id={}", userId);
        User user = userRepository.findById(userId).orElseThrow(() ->
                new EntityNotFoundException("Не существует пользователя с указанным id"));
        return requestMapper.convert(requestRepository.findAllByRequesterOrderById(userId));
    }

    @Override
    public RequestDto createRequest(Long userId, Long eventId) {
        log.info("Создание заявки на событие пользователем с id={}, id={} события", userId, eventId);
        User user = userRepository.findById(userId).orElseThrow(() ->
                new EntityNotFoundException("Не существует пользователя с указанным id"));
        Event event = eventRepository.findById(eventId).orElseThrow(() ->
                new UncorrectedParametersException("Не существует события с указанным id"));
        if (requestRepository.existsByRequesterAndEvent(userId, eventId)) {
            throw new UncorrectedParametersException("Событие уже создано");
        }
        if (event.getInitiator().getId().equals(userId)) {
            throw new UncorrectedParametersException("Нельзя записаться на собственное событие");
        }
        if (!event.getState().equals(EventState.PUBLISHED) || event.getState() == null) {
            throw new UncorrectedParametersException("Событие еще не создано");
        }
        List<Request> requests = requestRepository.findAllByEvent(eventId);
        if (!event.getRequestModeration() && requests.size() >= event.getParticipantLimit()) {
            throw new UncorrectedParametersException("Превышен лимит заявок");
        }
        Request request = Request.builder()
                .created(LocalDateTime.now())
                .event(event.getId())
                .requester(user.getId())
                .status(!event.getRequestModeration() || event.getParticipantLimit() == 0  ?
                        RequestStatus.CONFIRMED : RequestStatus.PENDING)
                .build();
        return requestMapper.convert(requestRepository.save(request));
    }

    @Override
    public RequestDto cancelRequest(Long userId, Long requestId) {
        log.info("Отмена заявки на событие с id={} пользователем с id={}", requestId, userId);
        /*User user = userRepository.findById(userId).orElseThrow(() ->
                new EntityNotFoundException("Не существует пользователя с указанным id"));*/
        Request request = requestRepository.findByRequesterAndId(userId, requestId).orElseThrow(() ->
                new EntityNotFoundException("Не существует запроса с указанным id"));
        request.setStatus(RequestStatus.CANCELED);
        return requestMapper.convert(requestRepository.save(request));
    }

    @Transactional(readOnly = true)
    @Override
    public List<RequestDto> getRequestsByOwner(Long userId, Long eventId) {
        log.info("Получение списка заявок на событие с id={}, id владельца={}", eventId, userId);
        User user = userRepository.findById(userId).orElseThrow(() ->
                new EntityNotFoundException("Не существует пользователя с указанным id"));
        List<Request> requests = requestRepository.findAllByEventWithInitiator(userId, eventId);
        return requestMapper.convert(requests);
    }

    @Override
    public EventRequestStatusUpdateResult updateRequest(Long userId, Long eventId, EventRequestStatusUpdateRequest request) {
        log.info("Изменение заявок события с id={}, пользователем с id={}", eventId, userId);
        log.info("Параметры: {}", request);
        User user = userRepository.findById(userId).orElseThrow(() ->
                new EntityNotFoundException("Не существует пользователя с указанным id"));
        Event event = eventRepository.findById(eventId).orElseThrow(() ->
                new EntityNotFoundException("Не существует события с указанным id"));
        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            return result;
        }
        List<Request> requests = requestRepository.findAllByEventWithInitiator(eventId, userId);
        List<Request> requestToUpdate = requests.stream()
                .filter(r -> request.getRequestIds().contains(r.getId()))
                .collect(Collectors.toList());
        if (requestToUpdate.stream().anyMatch(x -> x.getStatus().equals(RequestStatus.CONFIRMED) &&
                request.getStatus().equals(RequestStatusToUpdate.REJECTED))) {
            throw new UncorrectedParametersException("Заявка уже подтверждена");
        }

        if (event.getConfirmedRequests() != null && event.getConfirmedRequests() + requestToUpdate.size() > event.getParticipantLimit() &&
                request.getStatus().equals(RequestStatusToUpdate.CONFIRMED)) {
            throw new UncorrectedParametersException("Превышен лимит заявок");
        }
        for (Request r : requestToUpdate) {
            r.setStatus(RequestStatus.valueOf(request.getStatus().toString()));
        }
        requestRepository.saveAll(requestToUpdate);
        if (request.getStatus().equals(RequestStatusToUpdate.CONFIRMED)) {
            event.setConfirmedRequests(event.getConfirmedRequests() + requestToUpdate.size());
            eventRepository.save(event);
            result.setConfirmedRequests(requestMapper.convert(requestToUpdate));
        }
        if (request.getStatus().equals(RequestStatusToUpdate.REJECTED)) {
            result.setRejectedRequests(requestMapper.convert(requestToUpdate));
        }

        return result;
    }
}
