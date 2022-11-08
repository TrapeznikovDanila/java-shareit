package ru.practicum.shareit.booking.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.BookingStatus;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * TODO Sprint add-bookings.
 */
@Data
@NoArgsConstructor
public class BookingRequestDto {
    @NotNull
    private long id;
    @NotNull
    private long bookerId;
    @NotNull
    private long itemId;
    @NotBlank
    private String itemName;
    private BookingStatus status;
    @NotNull
    private LocalDateTime start;
    @NotNull
    private LocalDateTime end;
}
