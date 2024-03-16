package ru.practicum.ewm.mappers;

import org.mapstruct.Mapper;
import ru.practicum.ewm.dtos.event.LocationUpdateDto;
import ru.practicum.ewm.models.Location;

@Mapper(componentModel = "spring")
public interface LocationMapper {

    Location convert(LocationUpdateDto locationUpdateDto);
}
