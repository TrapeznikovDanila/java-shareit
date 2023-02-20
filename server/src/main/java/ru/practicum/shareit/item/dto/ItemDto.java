package ru.practicum.shareit.item.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingForItemDto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@Data
@NoArgsConstructor
public class ItemDto {
    private long id;
    @NotBlank
    private String name;
    @NotBlank
    private String description;
    @NotNull(message = "Field available can't be null")
    private Boolean available;
    private List<CommentsDto> comments;
    private int countOfBooking;
    private BookingForItemDto lastBooking;
    private BookingForItemDto nextBooking;
    private long requestId;
}
