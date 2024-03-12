package ru.practicum.ewm.mappers;

import org.mapstruct.Mapper;
import ru.practicum.ewm.dtos.user.UserDto;
import ru.practicum.ewm.models.User;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User convert(UserDto userDto);

    UserDto convert(User user);

    List<UserDto> convert(List<User> users);
}
