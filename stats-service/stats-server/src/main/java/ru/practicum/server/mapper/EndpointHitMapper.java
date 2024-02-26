package ru.practicum.server.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.server.model.EndpointHit;

@Mapper(componentModel = "spring")
public interface EndpointHitMapper {

    @Mapping(target = "timestamp", source = "timestamp")
    EndpointHit toEndpointHit(EndpointHitDto endpointHitDto);

    @Mapping(target = "timestamp", source = "timestamp")
    EndpointHitDto toEndpointHitDto(EndpointHit inputEndpointHit);
}
