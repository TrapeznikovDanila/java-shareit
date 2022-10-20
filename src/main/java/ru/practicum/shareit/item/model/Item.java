package ru.practicum.shareit.item.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * TODO Sprint add-controllers.
 */
@Data
@NoArgsConstructor
public class Item {
    private long id;
    private String name;
    private String description;
    @NotNull
    private long userId;
    @NotNull(message = "Field available can't be null")
    private Boolean available;
}
