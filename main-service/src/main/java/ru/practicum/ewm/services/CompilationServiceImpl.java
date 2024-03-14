package ru.practicum.ewm.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dtos.compilation.CompilationDto;
import ru.practicum.ewm.dtos.compilation.NewCompilationDto;
import ru.practicum.ewm.dtos.compilation.UpdateCompilationRequest;
import ru.practicum.ewm.exceptions.EntityNotFoundException;
import ru.practicum.ewm.mappers.CompilationMapper;
import ru.practicum.ewm.models.Compilation;
import ru.practicum.ewm.models.Event;
import ru.practicum.ewm.repositories.CompilationRepository;
import ru.practicum.ewm.repositories.EventRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CompilationMapper compilationMapper;
    //private final EventService eventService;

    @Override
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        Compilation compilation = new Compilation();
        if (newCompilationDto.getEvents() != null) {
            List<Event> events = eventRepository.findAllByIdIn(newCompilationDto.getEvents());
            compilation.setEvents(new HashSet<>(events));
        } else {
            compilation.setEvents(new HashSet<>());
        }
        //compilation.setEvents(new HashSet<>(events));
        if (newCompilationDto.getPinned() != null) {
            compilation.setPinned(newCompilationDto.getPinned());
        } else {
            compilation.setPinned(false);
        }
        compilation.setTitle(newCompilationDto.getTitle());
        Compilation compilationToSave = compilationRepository.save(compilation);
        log.info("Сохранена подборка: {}", compilationToSave);
        return compilationMapper.convert(compilationToSave);
    }

    @Override
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateCompilationRequest) {
        log.info("Обновление подборки c id = {}, параметры: {}", compId, updateCompilationRequest);
        Compilation compilation = compilationRepository.findById(compId).orElseThrow(() ->
                new EntityNotFoundException("Не существует подборки с указанным id"));
        List<Long> ids = updateCompilationRequest.getEvents();
        if (ids != null && !ids.isEmpty()) {
            List<Event> events = eventRepository.findAllByIdIn(ids);
            compilation.setEvents(new HashSet<>(events));
        }
        if (updateCompilationRequest.getPinned() != null) {
            compilation.setPinned(updateCompilationRequest.getPinned());
        }
        if (updateCompilationRequest.getTitle() != null) {
            compilation.setTitle(updateCompilationRequest.getTitle());
        }
        Compilation compilationToSave = compilationRepository.save(compilation);
        //setView(compilationToSave);
        log.info("Обновлена подборка подборка: {}", compilationToSave);
        return compilationMapper.convert(compilationRepository.save(compilationToSave));
    }

    @Override
    public void deleteCompilation(Long compId) {
        log.info("Удаление подборки с id={}", compId);
        Compilation compilation = compilationRepository.findById(compId).orElseThrow(() ->
                new EntityNotFoundException("Подборка с указанным id не существует"));
        compilationRepository.deleteById(compId);
    }

    @Transactional(readOnly = true)
    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        log.info("Получение списка подборок");
        Pageable page = PageRequest.of(from / size, size);
        if (pinned != null && pinned) {
            return compilationMapper.convert(compilationRepository.findAllByPinned(pinned, page));
        }
        return compilationMapper.convert(compilationRepository.findAll());
    }

    @Transactional(readOnly = true)
    @Override
    public CompilationDto getCompilation(Long compId) {
        log.info("Получение подборки с id={}", compId);
        Compilation compilation = compilationRepository.findById(compId).orElseThrow(() ->
                new EntityNotFoundException("Подборка с указанным id не существует"));

        return compilationMapper.convert(compilation);
    }

    /*private void setView(Compilation compilation) {
        Set<Event> setEvents = compilation.getEvents();
        if (!setEvents.isEmpty()) {
            List<Event> events = new ArrayList<>(setEvents);
            eventService.setView(events);
        }
    }*/
}
