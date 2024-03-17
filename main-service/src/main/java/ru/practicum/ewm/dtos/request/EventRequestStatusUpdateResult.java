package ru.practicum.ewm.dtos.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventRequestStatusUpdateResult {

    List<RequestDto> confirmedRequests;
    List<RequestDto> rejectedRequests;
}
