package ru.practicum.ewm.services;

import ru.practicum.ewm.dtos.compilation.CompilationDto;
import ru.practicum.ewm.dtos.compilation.NewCompilationDto;
import ru.practicum.ewm.dtos.compilation.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {

    CompilationDto createCompilation(NewCompilationDto newCompilationDto);

    CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateCompilationRequest);

    void deleteCompilation(Long compId);

    List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size);

    CompilationDto getCompilation(Long compId);
}
