package ru.practicum.ewm.services;

import ru.practicum.ewm.dtos.user.UserDto;

import java.util.List;

public interface UserService {

    List<UserDto> getUsers(List<Long> ids, Integer from, Integer size);

    UserDto createUser(UserDto userDto);

    void deleteUser(Long userId);
}
