package ru.practicum.ewm.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dtos.user.UserDto;
import ru.practicum.ewm.exceptions.EntityNotFoundException;
import ru.practicum.ewm.exceptions.UncorrectedParametersException;
import ru.practicum.ewm.mappers.UserMapper;
import ru.practicum.ewm.models.User;
import ru.practicum.ewm.repositories.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    @Override
    public List<UserDto> getUsers(List<Long> ids, Integer from, Integer size) {
        log.info("Получение списка пользователей");
        Pageable page = PageRequest.of(from / size, size);
        if (ids != null && !ids.isEmpty()) {
            return userMapper.convert(userRepository.findAllById(ids));
        }
        return userMapper.convert(userRepository.findAll(page).toList());
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        if (userRepository.existsByName(userDto.getName())) {
            throw new UncorrectedParametersException("Пользователь с заданным именем уже существует");
        }
        User userToSave = userRepository.save(userMapper.convert(userDto));
        log.info("Создан пользователь: {}", userToSave);
        return userMapper.convert(userToSave);
    }

    @Override
    public void deleteUser(Long userId) {
        log.info("Удаление пользователя с id = {}", userId);
        User user = userRepository.findById(userId).orElseThrow(() ->
                new EntityNotFoundException("Не существует пользователя с указанным id"));
        userRepository.deleteById(userId);
    }
}
