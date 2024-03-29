package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private final Map<String, BookingState> bookingStates = new HashMap<>();

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
                                                          @RequestParam(required = false) String state,
                                                          @RequestParam(required = false) Integer from,
                                                          @RequestParam(required = false) Integer size) {
        return bookingService.getBookingsByBookerId(bookerId, state, from, size);
    }

    @GetMapping("/owner")
    public List<BookingResponseDto> getBookingsForAllItemsByOwnerId(@RequestHeader("X-Sharer-User-Id") long userId,
                                                                    @RequestParam(required = false) String state,
                                                                    @RequestParam(required = false) Integer from,
                                                                    @RequestParam(required = false) Integer size) {
        fillMap();
        return bookingService.getBookingsForAllItemsByOwnerId(userId, bookingStates.get(state), from, size);
    }

    private Map<String, BookingState> fillMap() {
        bookingStates.put(null, BookingState.ALL);
        bookingStates.put("ALL", BookingState.ALL);
        bookingStates.put("PAST", BookingState.PAST);
        bookingStates.put("FUTURE", BookingState.FUTURE);
        bookingStates.put("CURRENT", BookingState.CURRENT);
        bookingStates.put("WAITING", BookingState.WAITING);
        bookingStates.put("REJECTED", BookingState.REJECTED);
        return bookingStates;
    }
}