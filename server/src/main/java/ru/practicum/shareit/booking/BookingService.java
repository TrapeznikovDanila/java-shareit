package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.util.List;

public interface BookingService {
    BookingResponseDto saveNewBooking(long bookerId, BookingRequestDto bookingDto);

    BookingResponseDto bookingConfirmation(long ownerId, long bookingId, Boolean approved);

    BookingResponseDto getBookingById(long userId, long bookingId);

    List<BookingResponseDto> getBookingsByBookerId(long bookerId, String state, Integer from, Integer size);

    List<BookingResponseDto> getBookingsForAllItemsByOwnerId(long userId, BookingState state, Integer from, Integer size);
}
