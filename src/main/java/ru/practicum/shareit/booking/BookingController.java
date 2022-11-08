package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.exception.ValidationException;

import java.util.List;

/**
 * TODO Sprint add-bookings.
 */
@Slf4j
@Validated
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingResponseDto saveNewBooking(@RequestHeader("X-Sharer-User-Id") long bookerId,
                                             @RequestBody BookingRequestDto bookingDto) {
        return bookingService.saveNewBooking(bookerId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto bookingConfirmation(@RequestHeader("X-Sharer-User-Id") long ownerId,
                                                  @PathVariable long bookingId,
                                                  @RequestParam Boolean approved) {
        return bookingService.bookingConfirmation(ownerId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingResponseDto getBookingById(@RequestHeader("X-Sharer-User-Id") long bookerId,
                                             @PathVariable long bookingId) {
        return bookingService.getBookingById(bookerId, bookingId);
    }

    @GetMapping
    public List<BookingResponseDto> getBookingsByBookerId(@RequestHeader("X-Sharer-User-Id") long bookerId,
                                                          @RequestParam(required = false) String state) {
        if (state == null || state.equals("ALL")) {
            return bookingService.getBookingsByBookerId(bookerId, BookingState.ALL);
        } else if (state.equals("PAST")) {
            return bookingService.getBookingsByBookerId(bookerId, BookingState.PAST);
        } else if (state.equals("FUTURE")) {
            return bookingService.getBookingsByBookerId(bookerId, BookingState.FUTURE);
        } else if (state.equals("CURRENT")) {
            return bookingService.getBookingsByBookerId(bookerId, BookingState.CURRENT);
        } else if (state.equals("WAITING")) {
            return bookingService.getBookingsByBookerId(bookerId, BookingState.WAITING);
        } else if (state.equals("REJECTED")) {
            return bookingService.getBookingsByBookerId(bookerId, BookingState.REJECTED);
        } else {
            log.error("Unknown state: " + state);
            throw new ValidationException("Unknown state: " + state);
        }
    }

    @GetMapping("/owner")
    public List<BookingResponseDto> getBookingsForAllItemsByOwnerId(@RequestHeader("X-Sharer-User-Id") long userId,
                                                                    @RequestParam(required = false) String state) {
        if (state == null || state.equals("ALL")) {
            return bookingService.getBookingsForAllItemsByOwnerId(userId, BookingState.ALL);
        } else if (state.equals("PAST")) {
            return bookingService.getBookingsForAllItemsByOwnerId(userId, BookingState.PAST);
        } else if (state.equals("FUTURE")) {
            return bookingService.getBookingsForAllItemsByOwnerId(userId, BookingState.FUTURE);
        } else if (state.equals("CURRENT")) {
            return bookingService.getBookingsForAllItemsByOwnerId(userId, BookingState.CURRENT);
        } else if (state.equals("WAITING")) {
            return bookingService.getBookingsForAllItemsByOwnerId(userId, BookingState.WAITING);
        } else if (state.equals("REJECTED")) {
            return bookingService.getBookingsForAllItemsByOwnerId(userId, BookingState.REJECTED);
        } else {
            log.error("Unknown state: " + state);
            throw new ValidationException("Unknown state: " + state);
        }
    }
}
