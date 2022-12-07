package ru.practicum.shareit.user.dto;

import lombok.Data;

import javax.validation.constraints.Email;

/**
 * TODO Sprint add-controllers.
 */
@Data
public class UserDto {
    private long id;
    @Email
    private String email;
    private String name;
}
