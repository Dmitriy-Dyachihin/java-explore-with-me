package ru.practicum.ewm.services;

import ru.practicum.ewm.dtos.comment.ShortCommentDto;
import ru.practicum.ewm.dtos.comment.CommentDto;

import java.util.List;

public interface CommentService {

    CommentDto createComment(ShortCommentDto commentCreateDto, Long userId, Long eventId);

    CommentDto updateComment(ShortCommentDto commentDto, Long userId, Long commentId);

    void deleteComment(Long userId, Long commentId);

    CommentDto getCommentByIdByUser(Long userId, Long commentId);

    CommentDto getCommentByIdByAdmin(Long commentId);

    List<CommentDto> getCommentsByEventIdByAdmin(Long eventId, Integer from, Integer size);

    void deleteCommentByAdmin(Long commentId);

    CommentDto updateCommentByAdmin(ShortCommentDto shortCommentDto, Long commentId);
}
