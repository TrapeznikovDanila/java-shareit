package ru.practicum.shareit.booking;

import lombok.Data;

import java.time.LocalDate;

/**
 * TODO Sprint add-bookings.
 */
@Data
public class Booking {
    private long bookingId;
    private LocalDate startBookingDate;
    private LocalDate finishBookingDate;
    private long itemId;
    private long userId;
    private String feedback;
}
