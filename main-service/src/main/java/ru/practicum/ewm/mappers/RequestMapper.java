package ru.practicum.ewm.mappers;

import org.mapstruct.Mapper;
import ru.practicum.ewm.dtos.request.RequestDto;
import ru.practicum.ewm.models.Request;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RequestMapper {

    List<RequestDto> convert(List<Request> requests);

    RequestDto convert(Request request);
}
