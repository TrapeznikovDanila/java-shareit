package ru.practicum.shareit.item.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TODO Sprint add-controllers.
 */
@Data
@NoArgsConstructor
public class ItemForRequestDto {
    private long id;
    private String name;
    private long ownerId;
    private String description;
    private Boolean available;
    private long requestId;
}
