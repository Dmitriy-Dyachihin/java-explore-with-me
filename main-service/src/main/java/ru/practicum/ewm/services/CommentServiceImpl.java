package ru.practicum.ewm.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dtos.comment.CommentDto;
import ru.practicum.ewm.dtos.comment.ShortCommentDto;
import ru.practicum.ewm.exceptions.EntityNotFoundException;
import ru.practicum.ewm.exceptions.UncorrectedParametersException;
import ru.practicum.ewm.mappers.CommentMapper;
import ru.practicum.ewm.models.Comment;
import ru.practicum.ewm.models.Event;
import ru.practicum.ewm.models.User;
import ru.practicum.ewm.repositories.CommentRepository;
import ru.practicum.ewm.repositories.EventRepository;
import ru.practicum.ewm.repositories.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final CommentRepository commentRepository;

    @Override
    public CommentDto createComment(ShortCommentDto shortCommentDto, Long userId, Long eventId) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new EntityNotFoundException("Не существует пользователя с указанным id"));
        Event event = eventRepository.findById(eventId).orElseThrow(() ->
                new EntityNotFoundException("Не существует события с указанным id"));
        Comment comment = commentMapper.convert(shortCommentDto);
        comment.setCreated(LocalDateTime.now());
        comment.setAuthor(user);
        comment.setEvent(event);
        Comment commentToSave = commentRepository.save(comment);
        log.info("Создан комментарий с id={}", commentToSave.getId());
        return commentMapper.convert(commentToSave);
    }

    @Override
    public CommentDto updateComment(ShortCommentDto shortCommentDto, Long userId, Long commentId) {
        log.info("Обновление комментария с id={}, пользоваетелем с id={}", commentId, userId);
        log.info("Параметры:{}", shortCommentDto);
        User user = userRepository.findById(userId).orElseThrow(() ->
                new EntityNotFoundException("Не существует пользователя с указанным id"));
        Comment comment = commentRepository.findById(commentId).orElseThrow(() ->
                new EntityNotFoundException("Не существует комментария с указанным id"));
        if (comment.getAuthor().getId().equals(userId)) {
            comment.setText(shortCommentDto.getText());
        } else {
            throw new UncorrectedParametersException("Данный комментарий был оставлен другим пользователем");
        }
        Comment commentToSave = commentRepository.save(comment);
        log.info("Комментарий с id={} был обновлен ", commentId);
        return commentMapper.convert(commentToSave);
    }

    @Override
    public void deleteComment(Long userId, Long commentId) {
        log.info("Удаление комментария с id={}, пользоваетелем с id={}", commentId, userId);
        User user = userRepository.findById(userId).orElseThrow(() ->
                new EntityNotFoundException("Не существует пользователя с указанным id"));
        Comment comment = commentRepository.findById(commentId).orElseThrow(() ->
                new EntityNotFoundException("Не существует комментария с указанным id"));
        if (comment.getAuthor().getId().equals(userId)) {
            commentRepository.deleteById(commentId);
        } else {
            throw new UncorrectedParametersException("Данный комментарий был оставлен другим пользователем");
        }
        log.info("Комментарий с id={} был удален ", commentId);
    }

    @Override
    public CommentDto getCommentByIdByUser(Long userId, Long commentId) {
        log.info("Получение комментария с id={}, пользоваетелем с id={}", commentId, userId);
        User user = userRepository.findById(userId).orElseThrow(() ->
                new EntityNotFoundException("Не существует пользователя с указанным id"));
        Comment comment = commentRepository.findById(commentId).orElseThrow(() ->
                new EntityNotFoundException("Не существует комментария с указанным id"));
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new UncorrectedParametersException("Данный комментарий был оставлен другим пользователем");
        }
        return commentMapper.convert(comment);
    }

    @Override
    public CommentDto getCommentByIdByAdmin(Long commentId) {
        log.info("Получение комментария с id={} администратором", commentId);
        Comment comment = commentRepository.findById(commentId).orElseThrow(() ->
                new EntityNotFoundException("Не существует комментария с указанным id"));
        return commentMapper.convert(comment);
    }

    @Override
    public List<CommentDto> getCommentsByEventIdByAdmin(Long eventId, Integer from, Integer size) {
        log.info("Получение комментариев событием с if={} администратором", eventId);
        log.info("Параметры: from={}, size+{}", from, size);
        Event event = eventRepository.findById(eventId).orElseThrow(() ->
                new EntityNotFoundException("Не существует события с указанным id"));
        Pageable page = PageRequest.of(from / size, size);
        List<Comment> comments = commentRepository.findAllByEvent_Id(eventId, page);
        return commentMapper.convert(comments);
    }

    @Override
    public void deleteCommentByAdmin(Long commentId) {
        log.info("Удаление комментария с id={} администратором", commentId);
        Comment comment = commentRepository.findById(commentId).orElseThrow(() ->
                new EntityNotFoundException("Не существует комментария с указанным id"));
        commentRepository.deleteById(commentId);
    }

    @Override
    public CommentDto updateCommentByAdmin(ShortCommentDto shortCommentDto, Long commentId) {
        log.info("Обновление комментария с id={}, администратором", commentId);
        log.info("Параметры:{}", shortCommentDto);
        Comment comment = commentRepository.findById(commentId).orElseThrow(() ->
                new EntityNotFoundException("Не существует комментария с указанным id"));
        comment.setText(shortCommentDto.getText());
        Comment commentToSave = commentRepository.save(comment);
        log.info("Комментарий с id={} был обновлен ", commentId);
        return commentMapper.convert(commentToSave);
    }
}
