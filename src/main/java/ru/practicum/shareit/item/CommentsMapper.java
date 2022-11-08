package ru.practicum.shareit.item;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.CommentsDto;
import ru.practicum.shareit.item.model.Comments;

@Component
public class CommentsMapper {
    public static Comments makeComment(CommentsDto commentDto) {
        Comments comment = new Comments();
        comment.setId(commentDto.getId());
        comment.setItemId(commentDto.getItemId());
        comment.setText(commentDto.getText());
        comment.setCreated(commentDto.getCreated());
        comment.setAuthorName(commentDto.getAuthorName());
        return comment;
    }

    public static CommentsDto makeCommentDto(Comments comment) {
        CommentsDto commentDto = new CommentsDto();
        commentDto.setId(comment.getId());
        commentDto.setItemId(comment.getItemId());
        commentDto.setText(comment.getText());
        commentDto.setCreated(comment.getCreated());
        commentDto.setAuthorName(comment.getAuthorName());
        return commentDto;
    }
}
