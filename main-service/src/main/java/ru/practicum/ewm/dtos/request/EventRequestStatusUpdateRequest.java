package ru.practicum.ewm.dtos.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewm.enums.RequestStatusToUpdate;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventRequestStatusUpdateRequest {

    List<Long> requestIds;
    RequestStatusToUpdate status;
}
