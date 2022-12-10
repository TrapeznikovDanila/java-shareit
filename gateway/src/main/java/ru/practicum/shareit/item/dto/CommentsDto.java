package ru.practicum.shareit.item.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * TODO Sprint add-controllers.
 */
@Data
@NoArgsConstructor
public class CommentsDto {
    private long id;
    private long itemId;
    private String authorName;
    @NotBlank(message = "Text can't be empty")
    private String text;
    private LocalDateTime created;
}
