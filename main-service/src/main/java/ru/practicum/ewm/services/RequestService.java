package ru.practicum.ewm.services;

import ru.practicum.ewm.dtos.request.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.dtos.request.EventRequestStatusUpdateResult;
import ru.practicum.ewm.dtos.request.RequestDto;

import java.util.List;

public interface RequestService {

    List<RequestDto> getCurrentUserRequests(Long userId);

    RequestDto createRequest(Long userId, Long eventId);

    RequestDto cancelRequest(Long userId, Long requestId);

    List<RequestDto> getRequestsByOwner(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateRequest(Long userId, Long eventId, EventRequestStatusUpdateRequest request);
}
