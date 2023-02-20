package ru.practicum.shareit.user.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

/**
 * TODO Sprint add-controllers.
 */
@Data
public class UserDto {
    private long id;
    @Email
    @NotNull
    private String email;
    @NotNull
    private String name;
}
