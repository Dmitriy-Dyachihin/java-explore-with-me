package ru.practicum.ewm.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.ewm.dtos.comment.CommentDto;
import ru.practicum.ewm.dtos.comment.ShortCommentDto;
import ru.practicum.ewm.models.Comment;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    Comment convert(ShortCommentDto shortCommentDto);

    @Mapping(source = "author.name", target = "authorName")
    @Mapping(source = "event.id", target = "eventId")
    CommentDto convert(Comment comment);

    List<CommentDto> convert(List<Comment> comments);
}
