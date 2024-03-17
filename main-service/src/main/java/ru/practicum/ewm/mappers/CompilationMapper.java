package ru.practicum.ewm.mappers;

import org.mapstruct.Mapper;
import ru.practicum.ewm.dtos.compilation.CompilationDto;
import ru.practicum.ewm.models.Compilation;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CompilationMapper {

    CompilationDto convert(Compilation compilation);

    List<CompilationDto> convert(List<Compilation> compilations);
}
