package ru.practicum.ewm.mappers;

import org.mapstruct.Mapper;
import ru.practicum.ewm.dtos.category.CategoryDto;
import ru.practicum.ewm.dtos.category.NewCategoryDto;
import ru.practicum.ewm.models.Category;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    Category convert(NewCategoryDto newCategoryDto);

    CategoryDto convert(Category category);

    List<CategoryDto> convert(List<Category> categories);
}
