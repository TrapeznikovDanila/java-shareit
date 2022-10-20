package ru.practicum.shareit.booking.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * TODO Sprint add-bookings.
 */
@Data
public class BookingDto {
    private long bookingDtoId;
    private LocalDate startBookingDate;
    private LocalDate finishBookingDate;
    private long itemId;
    private long userId;
    private String feedback;
}
