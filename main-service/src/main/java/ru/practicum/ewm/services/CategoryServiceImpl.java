package ru.practicum.ewm.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dtos.category.CategoryDto;
import ru.practicum.ewm.dtos.category.NewCategoryDto;
import ru.practicum.ewm.exceptions.EntityNotFoundException;
import ru.practicum.ewm.exceptions.UncorrectedParametersException;
import ru.practicum.ewm.mappers.CategoryMapper;
import ru.practicum.ewm.models.Category;
import ru.practicum.ewm.repositories.CategoryRepository;
import ru.practicum.ewm.repositories.EventRepository;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Transactional
    @Override
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        if (categoryRepository.findByNameIgnoreCase(newCategoryDto.getName()).isPresent()) {
            throw new UncorrectedParametersException("Категория с указанным именем уже существует");
        }
        Category category = categoryRepository.save(categoryMapper.convert(newCategoryDto));
        log.info("Создана новая категория: {}", category);
        return categoryMapper.convert(category);
    }

    @Transactional
    @Override
    public CategoryDto updateCategory(Long catId, CategoryDto categoryDto) {
        log.info("Редактирование категории с id:{}", catId);
        Category category =  categoryRepository.findById(catId)
                .orElseThrow(() -> new EntityNotFoundException("Не существует сущности с указанным id"));
        if (!category.getName().equals(categoryDto.getName())) {
            if (categoryRepository.findByNameIgnoreCase(categoryDto.getName()).isPresent()) {
                throw new UncorrectedParametersException("Категория с указанным именем уже существует");
            }
        }
        category.setName(categoryDto.getName());
        return categoryMapper.convert(categoryRepository.save(category));
    }

    @Transactional
    @Override
    public void deleteCategory(Long catId) {
        log.info("Удаление категории с id:{}", catId);
        Category category =  categoryRepository.findById(catId)
                .orElseThrow(() -> new EntityNotFoundException("Не существует сущности с указанным id"));
        if (eventRepository.existsByCategoryId(catId)) {
            throw new UncorrectedParametersException("Существуют события, связанные с категорией");
        }
        categoryRepository.deleteById(catId);
    }

    @Override
    public List<CategoryDto> getCategories(Integer from, Integer size) {
        log.info("Получение  списка категорий");
        Pageable page = PageRequest.of(from / size, size);
        List<Category> categories = categoryRepository.findAll(page).toList();
        if (categories.isEmpty()) {
            return Collections.emptyList();
        }
        return categoryMapper.convert(categories);
    }

    @Override
    public CategoryDto getCategoryById(Long catId) {
        log.info("Получение категории с id = {}", catId);
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new EntityNotFoundException("Категория не найдена"));
        return categoryMapper.convert(category);
    }
}
