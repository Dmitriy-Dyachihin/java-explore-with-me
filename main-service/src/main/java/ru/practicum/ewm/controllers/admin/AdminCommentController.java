package ru.practicum.ewm.controllers.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.dtos.comment.CommentDto;
import ru.practicum.ewm.dtos.comment.ShortCommentDto;
import ru.practicum.ewm.services.CommentService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import java.util.List;

@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
@Validated
public class AdminCommentController {

    private final CommentService commentService;

    @GetMapping("/{commentId}")
    public CommentDto getCommentById(@PathVariable Long commentId) {
        return commentService.getCommentByIdByAdmin(commentId);
    }

    @GetMapping
    public List<CommentDto> getCommentsByEventId(@Positive @RequestParam Long eventId,
                                                 @RequestParam(value = "from", defaultValue = "0") @Min(0) Integer from,
                                                 @RequestParam(value = "size", defaultValue = "10") @Min(1) Integer size) {
        return commentService.getCommentsByEventIdByAdmin(eventId, from, size);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable(value = "commentId") Long commentId) {
        commentService.deleteCommentByAdmin(commentId);
    }

    @PatchMapping("/{commentId}")
    public CommentDto updateComment(@Valid @RequestBody ShortCommentDto shortCommentDto,
                                    @PathVariable(value = "commentId") Long commentId) {
        return commentService.updateCommentByAdmin(shortCommentDto, commentId);
    }
}
