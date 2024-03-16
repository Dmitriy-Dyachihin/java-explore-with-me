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
import java.util.ArrayList;
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
                new EntityNotFoundException("Не существует события с указанным id"));
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
        List<Request> requests = requestRepository.findAllByEventAndInitiator(userId, eventId);
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

        List<Request> requests = requestRepository.findAllByEventAndInitiator(userId, eventId);
        List<Request> requestToUpdate = requests.stream()
                .filter(r -> request.getRequestIds().contains(r.getId()))
                .collect(Collectors.toList());

        List<Request> confirmedRequests = new ArrayList<>();
        List<Request> rejectedRequests = new ArrayList<>();


        if (request.getRequestIds() != null && !request.getRequestIds().isEmpty()) {
            for (Request r : requestToUpdate) {
                if (!r.getStatus().equals(RequestStatus.PENDING)) {
                    throw new UncorrectedParametersException("Статус можно изменить только у заявок, находящихся в состоянии ожидания");
                }
                if (request.getStatus().equals(RequestStatusToUpdate.REJECTED)) {
                    rejectedRequests.add(r);
                }
                if (request.getStatus().equals(RequestStatusToUpdate.CONFIRMED)) {

                    if (event.getConfirmedRequests() != null) {
                        if (event.getParticipantLimit() >= event.getConfirmedRequests() + 1 ||
                                event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
                            confirmedRequests.add(r);
                            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
                        } else {
                            rejectedRequests.add(r);
                        }
                    } else {
                        if (event.getParticipantLimit() >= 1 || event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
                            confirmedRequests.add(r);
                            event.setConfirmedRequests(1L);
                        } else {
                            rejectedRequests.add(r);
                        }
                    }
                }
            }
        }

        confirmedRequests.forEach(r -> r.setStatus(RequestStatus.CONFIRMED));
        rejectedRequests.forEach(r -> r.setStatus(RequestStatus.REJECTED));
        result.setRejectedRequests(requestMapper.convert(rejectedRequests));
        result.setConfirmedRequests(requestMapper.convert(confirmedRequests));

        return result;

    }
}
