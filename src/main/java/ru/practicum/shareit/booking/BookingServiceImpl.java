package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Comparator;
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
            throw new NotFoundException("The user cannot rent his items");
        }
        if (item.getAvailable() == true) {
            bookingRequestDto.setBookerId(bookerId);
            bookingRequestDto.setStatus(BookingStatus.WAITING);
            Booking booking = bookingRepository.save(BookingMapper.makeBooking(bookingRequestDto));
            booking.setItem(itemService.getItemById(bookingRequestDto.getItemId()));
            booking.setBooker(UserMapper.makeUser(userService.getUserById(bookingRequestDto.getBookerId())));
            BookingResponseDto bookingResponseDto = BookingMapper.makeBookingResponseDto(booking);
            return bookingResponseDto;
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
        if ((approved == true) && (booking.getStatus().equals(BookingStatus.APPROVED))) {
            throw new ValidationException("Approved error");
        }
        if (approved != null) {
            if (approved == true) {
                booking.setStatus(BookingStatus.APPROVED);
            } else {
                booking.setStatus(BookingStatus.REJECTED);
            }
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
    public List<BookingResponseDto> getBookingsByBookerId(long bookerId, BookingState state) {
        itemService.checkUserId(bookerId);
        if (state == BookingState.CURRENT) {
            return getCurrentBookingsByBookerId(bookerId);
        } else if (state == BookingState.FUTURE) {
            return getFutureBookingsByBookerId(bookerId);
        } else if (state == BookingState.PAST) {
            return getPastBookingsByBookerId(bookerId);
        } else if (state == BookingState.REJECTED) {
            return getRejectedBookingsByBookerId(bookerId);
        } else if (state == BookingState.WAITING) {
            return getWaitingBookingsByBookerId(bookerId);
        } else {
            return getAllBookingsByBookerId(bookerId);
        }
    }

    @Override
    public List<BookingResponseDto> getBookingsForAllItemsByOwnerId(long userId, BookingState state) {
        itemService.checkUserId(userId);
        if (state == BookingState.CURRENT) {
            return getCurrentBookingsOfAllItemsByOwnerId(userId);
        } else if (state == BookingState.FUTURE) {
            return getFutureBookingsOfAllItemsByOwnerId(userId);
        } else if (state == BookingState.PAST) {
            return getPastBookingsOfAllItemsByOwnerId(userId);
        } else if (state == BookingState.REJECTED) {
            return getRejectedBookingsOfAllItemsByOwnerId(userId);
        } else if (state == BookingState.WAITING) {
            return getWaitingBookingsOfAllItemsByOwnerId(userId);
        } else {
            return getAllBookingsOfAllItemsByOwnerId(userId);
        }
    }

    private Booking getBooking(long bookingId) {
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

    private List<BookingResponseDto> getAllBookingsByBookerId(long bookerId) {
        return bookingRepository.findByBookerId(bookerId)
                .stream()
                .map(BookingMapper::makeBookingResponseDto)
                .sorted((b1, b2) -> b2.getStart().compareTo(b1.getStart()))
                .collect(Collectors.toList());
    }

    private List<BookingResponseDto> getCurrentBookingsByBookerId(long bookerId) {
        return bookingRepository.findByBookerId(bookerId)
                .stream()
                .filter(booking -> booking.getStart().isBefore(LocalDateTime.now()))
                .filter(booking -> booking.getEnd().isAfter(LocalDateTime.now()))
                .map(BookingMapper::makeBookingResponseDto)
                .sorted((b1, b2) -> b2.getStart().compareTo(b1.getStart()))
                .collect(Collectors.toList());
    }

    private List<BookingResponseDto> getFutureBookingsByBookerId(long bookerId) {
        return bookingRepository.findByBookerId(bookerId)
                .stream()
                .map(BookingMapper::makeBookingResponseDto)
                .filter(booking -> booking.getStart().isAfter(LocalDateTime.now()))
                .sorted((b1, b2) -> b2.getStart().compareTo(b1.getStart()))
                .collect(Collectors.toList());
    }

    private List<BookingResponseDto> getPastBookingsByBookerId(long bookerId) {
        return bookingRepository.findByBookerId(bookerId)
                .stream()
                .filter(booking -> booking.getEnd().isBefore(LocalDateTime.now()))
                .map(BookingMapper::makeBookingResponseDto)
                .sorted(Comparator.comparing(BookingResponseDto::getStart))
                .collect(Collectors.toList());
    }

    private List<BookingResponseDto> getRejectedBookingsByBookerId(long bookerId) {
        return bookingRepository.findByBookerId(bookerId)
                .stream()
                .map(BookingMapper::makeBookingResponseDto)
                .filter(booking -> booking.getStatus() == BookingStatus.REJECTED)
                .sorted((b1, b2) -> b2.getStart().compareTo(b1.getStart()))
                .collect(Collectors.toList());
    }

    private List<BookingResponseDto> getWaitingBookingsByBookerId(long bookerId) {
        return bookingRepository.findByBookerId(bookerId)
                .stream()
                .map(BookingMapper::makeBookingResponseDto)
                .filter(booking -> booking.getStatus() == BookingStatus.WAITING)
                .sorted((b1, b2) -> b2.getStart().compareTo(b1.getStart()))
                .collect(Collectors.toList());
    }

    private List<BookingResponseDto> getAllBookingsOfAllItemsByOwnerId(long userId) {
        return bookingRepository.findBookingsByBookerIdJoinItem(userId)
                .stream()
                .map(BookingMapper::makeBookingResponseDto)
                .collect(Collectors.toList());
    }

    private List<BookingResponseDto> getCurrentBookingsOfAllItemsByOwnerId(long userId) {
        return bookingRepository.findBookingsByBookerIdJoinItem(userId)
                .stream()
                .filter(b -> b.getStart().isBefore(LocalDateTime.now()))
                .filter(b -> b.getEnd().isAfter(LocalDateTime.now()))
                .map(BookingMapper::makeBookingResponseDto)
                .collect(Collectors.toList());
    }

    private List<BookingResponseDto> getFutureBookingsOfAllItemsByOwnerId(long userId) {
        return bookingRepository.findBookingsByBookerIdJoinItem(userId)
                .stream()
                .map(BookingMapper::makeBookingResponseDto)
                .filter(b -> b.getStart().isAfter(LocalDateTime.now()))
                .collect(Collectors.toList());
    }

    private List<BookingResponseDto> getPastBookingsOfAllItemsByOwnerId(long userId) {
        return bookingRepository.findBookingsByBookerIdJoinItem(userId)
                .stream()
                .filter(b -> b.getStatus() == BookingStatus.APPROVED)
                .filter(b -> b.getEnd().isBefore(LocalDateTime.now()))
                .map(BookingMapper::makeBookingResponseDto)
                .collect(Collectors.toList());
    }

    private List<BookingResponseDto> getRejectedBookingsOfAllItemsByOwnerId(long userId) {
        return bookingRepository.findBookingsByBookerIdJoinItem(userId)
                .stream()
                .map(BookingMapper::makeBookingResponseDto)
                .filter(b -> b.getStatus() == BookingStatus.REJECTED)
                .collect(Collectors.toList());
    }

    private List<BookingResponseDto> getWaitingBookingsOfAllItemsByOwnerId(long userId) {
        return bookingRepository.findBookingsByBookerIdJoinItem(userId)
                .stream()
                .map(BookingMapper::makeBookingResponseDto)
                .filter(b -> b.getStatus() == BookingStatus.WAITING)
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
}
