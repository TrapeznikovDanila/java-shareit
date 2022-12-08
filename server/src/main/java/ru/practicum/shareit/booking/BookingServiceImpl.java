package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;

    private final ItemService itemService;

    private final UserService userService;

    @Override
    public BookingResponseDto saveNewBooking(long bookerId, BookingRequestDto bookingRequestDto) {
        itemService.checkUserId(bookerId);
        validation(bookingRequestDto);
        Item item = itemService.getItemById(bookingRequestDto.getItemId());
        if (item.getUserId() == bookerId) {
            throw new NotFoundException("This item already belongs to you, " +
                    "so you can't rent it");
        }
        if (item.getAvailable() == true) {
            bookingRequestDto.setBookerId(bookerId);
            bookingRequestDto.setStatus(BookingStatus.WAITING);
            Booking booking = bookingRepository.save(BookingMapper.makeBooking(bookingRequestDto));
            booking.setItem(itemService.getItemById(bookingRequestDto.getItemId()));
            booking.setBooker(UserMapper.makeUser(userService.getUserById(bookingRequestDto.getBookerId())));
            return BookingMapper.makeBookingResponseDto(booking);
        } else {
            log.error("This item isn't available");
            throw new ValidationException("This item isn't available");
        }

    }

    @Override
    public BookingResponseDto bookingConfirmation(long ownerId, long bookingId, Boolean approved) {
        itemService.checkUserId(ownerId);
        Booking booking = getBooking(bookingId);
        booking.setItem(itemService.getItemById(booking.getItem().getId()));
        booking.setBooker(UserMapper.makeUser(userService.getUserById(booking.getBooker().getId())));
        itemService.checkOwner(ownerId, booking.getItem().getId());
        if (approved && booking.getStatus().equals(BookingStatus.APPROVED)) {
            throw new ValidationException("Approved error");
        }
        if (approved) {
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }
        return BookingMapper.makeBookingResponseDto(bookingRepository.save(booking));
    }

    @Override
    public BookingResponseDto getBookingById(long userId, long bookingId) {
        itemService.checkUserId(userId);
        Booking booking = getBooking(bookingId);
        if (userId == booking.getBooker().getId()) {
            return BookingMapper.makeBookingResponseDto(booking);
        } else {
            itemService.checkOwner(userId, booking.getItem().getId());
            return BookingMapper.makeBookingResponseDto(booking);
        }
    }

    @Override
    public List<BookingResponseDto> getBookingsByBookerId(long bookerId, String state, Integer from, Integer size) {
        itemService.checkUserId(bookerId);
        if (from == null) {
            from = 0;
        }
        if (size == null) {
            size = 10;
        }
        pageParametersValidation(from, size);
        int page = calculatePage(from, size);
        if (getBookingState(state).equals(BookingState.CURRENT)) {
            return getCurrentBookingsByBookerId(bookerId, page, size);
        } else if (getBookingState(state).equals(BookingState.FUTURE)) {
            return getFutureBookingsByBookerId(bookerId, page, size);
        } else if (getBookingState(state).equals(BookingState.PAST)) {
            return getPastBookingsByBookerId(bookerId, page, size);
        } else if (getBookingState(state).equals(BookingState.REJECTED)) {
            return getRejectedBookingsByBookerId(bookerId, page, size);
        } else if (getBookingState(state).equals(BookingState.WAITING)) {
            return getWaitingBookingsByBookerId(bookerId, page, size);
        } else {
            return getAllBookingsByBookerId(bookerId, page, size);
        }
    }

    @Override
    public List<BookingResponseDto> getBookingsForAllItemsByOwnerId(long userId, BookingState state, Integer from,
                                                                    Integer size) {
        itemService.checkUserId(userId);
        if (from == null) {
            from = 0;
        }
        if (size == null) {
            size = 10;
        }
        pageParametersValidation(from, size);
        int page = calculatePage(from, size);
        if (state == null) {
            state = BookingState.ALL;
        }
        if (state == BookingState.CURRENT) {
            return getCurrentBookingsOfAllItemsByOwnerId(userId, page, size);
        } else if (state.equals(BookingState.FUTURE)) {
            return getFutureBookingsOfAllItemsByOwnerId(userId, page, size);
        } else if (state.equals(BookingState.PAST)) {
            return getPastBookingsOfAllItemsByOwnerId(userId, page, size);
        } else if (state.equals(BookingState.REJECTED)) {
            return getRejectedBookingsOfAllItemsByOwnerId(userId, page, size);
        } else if (state.equals(BookingState.WAITING)) {
            return getWaitingBookingsOfAllItemsByOwnerId(userId, page, size);
        } else {
            return getAllBookingsOfAllItemsByOwnerId(userId, page, size);
        }
    }

    public Booking getBooking(long bookingId) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            booking.setItem(itemService.getItemById(booking.getItem().getId()));
            booking.setBooker(UserMapper.makeUser(userService.getUserById(booking.getBooker().getId())));
            return booking;
        } else {
            throw new NotFoundException("Booking id error");
        }
    }

    private List<BookingResponseDto> getAllBookingsByBookerId(long bookerId, int page, int size) {
        return bookingRepository.findByBookerId(bookerId, PageRequest.of(page, size,
                        Sort.Direction.DESC, "start"))
                .stream()
                .map(BookingMapper::makeBookingResponseDto)
                .collect(Collectors.toList());
    }

    private List<BookingResponseDto> getCurrentBookingsByBookerId(long bookerId, int page, int size) {
        return bookingRepository.findCurrentBookingsByBookerId(bookerId, LocalDateTime.now(),
                        PageRequest.of(page, size))
                .stream()
                .map(BookingMapper::makeBookingResponseDto)
                .collect(Collectors.toList());
    }

    private List<BookingResponseDto> getFutureBookingsByBookerId(long bookerId, int page, int size) {
        return bookingRepository.findFutureBookingsByBookerId(bookerId, LocalDateTime.now(),
                        PageRequest.of(page, size))
                .stream()
                .map(BookingMapper::makeBookingResponseDto)
                .collect(Collectors.toList());
    }

    private List<BookingResponseDto> getPastBookingsByBookerId(long bookerId, int page, int size) {
        return bookingRepository.findPastBookingsByBookerId(bookerId, LocalDateTime.now(),
                        PageRequest.of(page, size))
                .stream()
                .map(BookingMapper::makeBookingResponseDto)
                .collect(Collectors.toList());
    }

    private List<BookingResponseDto> getRejectedBookingsByBookerId(long bookerId, int page, int size) {
        return bookingRepository.findBookingByBooker_IdAndStatusIs(bookerId, BookingStatus.REJECTED,
                        PageRequest.of(page, size, Sort.Direction.DESC, "start"))
                .stream()
                .map(BookingMapper::makeBookingResponseDto)
                .collect(Collectors.toList());
    }

    private List<BookingResponseDto> getWaitingBookingsByBookerId(long bookerId, int page, int size) {
        return bookingRepository.findBookingByBooker_IdAndStatusIs(bookerId, BookingStatus.WAITING,
                        PageRequest.of(page, size, Sort.Direction.DESC, "start"))
                .stream()
                .map(BookingMapper::makeBookingResponseDto)
                .collect(Collectors.toList());
    }

    private List<BookingResponseDto> getAllBookingsOfAllItemsByOwnerId(long userId, int page, int size) {
        return bookingRepository.findByItem_UserIdOrderByStartDesc(userId, PageRequest.of(page, size))
                .stream()
                .map(BookingMapper::makeBookingResponseDto)
                .collect(Collectors.toList());
    }

    private List<BookingResponseDto> getCurrentBookingsOfAllItemsByOwnerId(long userId, int page, int size) {
        return bookingRepository.findByItem_userIdAndEndAfterAndStartBeforeOrderByStartDesc(userId, LocalDateTime.now(),
                        LocalDateTime.now(), PageRequest.of(page, size))
                .stream()
                .map(BookingMapper::makeBookingResponseDto)
                .collect(Collectors.toList());
    }

    private List<BookingResponseDto> getFutureBookingsOfAllItemsByOwnerId(long userId, int page, int size) {
        return bookingRepository.findByItem_userIdAndStartAfterOrderByStartDesc(userId, LocalDateTime.now(),
                        PageRequest.of(page, size))
                .stream()
                .map(BookingMapper::makeBookingResponseDto)
                .collect(Collectors.toList());
    }

    private List<BookingResponseDto> getPastBookingsOfAllItemsByOwnerId(long userId, int page, int size) {
        return bookingRepository.findByItem_userIdAndEndBeforeOrderByStartDesc(userId, LocalDateTime.now(),
                        PageRequest.of(page, size))
                .stream()
                .map(BookingMapper::makeBookingResponseDto)
                .collect(Collectors.toList());
    }

    private List<BookingResponseDto> getRejectedBookingsOfAllItemsByOwnerId(long userId, int page, int size) {
        return bookingRepository.findByItem_UserIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED,
                        PageRequest.of(page, size))
                .stream()
                .map(BookingMapper::makeBookingResponseDto)
                .collect(Collectors.toList());
    }

    private List<BookingResponseDto> getWaitingBookingsOfAllItemsByOwnerId(long userId, int page, int size) {
        return bookingRepository.findByItem_UserIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING,
                        PageRequest.of(page, size))
                .stream()
                .map(BookingMapper::makeBookingResponseDto)
                .collect(Collectors.toList());
    }

    private void validation(BookingRequestDto booking) {
        if (booking.getStart() == null) {
            log.error("The booking start time field cannot be empty");
            throw new ValidationException("The booking start time field cannot be empty");
        }
        if (booking.getEnd() == null) {
            log.error("The booking end time field cannot be empty");
            throw new ValidationException("The booking end time field cannot be empty");
        }
        if (booking.getStart().isBefore(LocalDateTime.now())) {
            log.error("The booking start time can't be in past");
            throw new ValidationException("The booking start time can't be in past");
        }
        if (booking.getEnd().isBefore(LocalDateTime.now())) {
            log.error("The booking end time can't be in past");
            throw new ValidationException("The booking end time can't be in past");
        }
        if (booking.getEnd().isBefore(booking.getStart())) {
            log.error("The booking end time must be after then start time");
            throw new ValidationException("The booking end time must be after then start time");
        }
        if (booking.getStart().isEqual(booking.getEnd())) {
            log.error("The booking start time and end time can't be the same");
            throw new ValidationException("The booking start time and end time can't be the same");
        }
        if (booking.getItemId() <= 0) {
            log.error("The item id error");
            throw new ValidationException("The item id error");
        }
    }

    private void pageParametersValidation(int from, int size) {
        if (from < 0) {
            throw new ValidationException("The from parameter can't be negative number");
        } else if (size <= 0) {
            throw new ValidationException("The size parameter must be positive number");
        }
    }

    private int calculatePage(int from, int size) {
        return from / size;
    }

    private BookingState getBookingState(String state) {
        if (state == null || state.equals("ALL")) {
            return BookingState.ALL;
        } else if (state.equals("PAST")) {
            return BookingState.PAST;
        } else if (state.equals("FUTURE")) {
            return BookingState.FUTURE;
        } else if (state.equals("CURRENT")) {
            return BookingState.CURRENT;
        } else if (state.equals("WAITING")) {
            return BookingState.WAITING;
        } else if (state.equals("REJECTED")) {
            return BookingState.REJECTED;
        } else {
            log.error("Unknown state: " + state);
            throw new ValidationException("Unknown state: " + state);
        }
    }
}
