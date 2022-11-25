package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.util.List;

public interface BookingService {
    BookingResponseDto saveNewBooking(long bookerId, BookingRequestDto bookingDto);

    BookingResponseDto bookingConfirmation(long ownerId, long bookingId, Boolean approved);

    BookingResponseDto getBookingById(long userId, long bookingId);

    List<BookingResponseDto> getBookingsByBookerId(long bookerId, BookingState state);

    List<BookingResponseDto> getBookingsByBookerId(long bookerId, BookingState state, int from, int size);

    List<BookingResponseDto> getBookingsForAllItemsByOwnerId(long userId, BookingState state);

    List<BookingResponseDto> getBookingsForAllItemsByOwnerId(long userId, BookingState state, int from, int size);
}
