package ru.practicum.ewm.controllers.priv;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.dtos.comment.ShortCommentDto;
import ru.practicum.ewm.dtos.comment.CommentDto;
import ru.practicum.ewm.services.CommentService;

import javax.validation.Valid;

@RestController
@RequestMapping("/users/{userId}/comments")
@RequiredArgsConstructor
@Validated
public class PrivateCommentController {

    private final CommentService commentService;

    @PostMapping("/{eventId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createComment(@Valid @RequestBody ShortCommentDto shortCommentDto, @PathVariable Long userId,
                                    @PathVariable Long eventId) {
        return commentService.createComment(shortCommentDto, userId, eventId);
    }

    @PatchMapping("/{commentId}")
    public CommentDto updateComment(@Valid @RequestBody ShortCommentDto shortCommentDto, @PathVariable Long userId,
                                    @PathVariable Long commentId) {
        return commentService.updateComment(shortCommentDto, userId, commentId);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteComment(@PathVariable Long userId, @PathVariable Long commentId) {
        commentService.deleteComment(userId, commentId);
    }

    @GetMapping("/{commentId}")
    public CommentDto getCommentById(@PathVariable Long userId, @PathVariable Long commentId) {
        return commentService.getCommentByIdByUser(userId, commentId);
    }
}
