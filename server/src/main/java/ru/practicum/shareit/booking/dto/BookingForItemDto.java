package ru.practicum.shareit.booking.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.BookingStatus;

import java.time.LocalDateTime;

/**
 * TODO Sprint add-bookings.
 */
@Data
@NoArgsConstructor
public class BookingForItemDto {
    private long id;
    private long bookerId;
    private long itemId;
    private BookingStatus status;
    private LocalDateTime start;
    private LocalDateTime end;
}
