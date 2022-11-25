package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemServiceImpl;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserServiceImpl;
import ru.practicum.shareit.user.dto.UserDto;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingServiceImplIntegrationTest {

    private final EntityManager em;

    private final BookingServiceImpl service;

    private final ItemServiceImpl itemService;

    private final UserServiceImpl userService;

    private final BookingRepository repository;

    private UserDto userDtoSaved1;

    private UserDto userDtoSaved2;

    private ItemDto itemFromService;

    private ItemDto itemFromService2;

    @BeforeEach
    void saveUsersAndItem() {
        UserDto userDtoNotSaved1 = new UserDto();
        userDtoNotSaved1.setName("Name");
        userDtoNotSaved1.setEmail("e@mail.ru");
        userDtoSaved1 = userService.saveNewUser(userDtoNotSaved1);

        UserDto userDtoNotSaved2 = new UserDto();
        userDtoNotSaved2.setName("Name2");
        userDtoNotSaved2.setEmail("e2@mail.ru");
        userDtoSaved2 = userService.saveNewUser(userDtoNotSaved2);

        ItemDto itemDto = new ItemDto();
        itemDto.setName("Name");
        itemDto.setDescription("Description");
        itemDto.setAvailable(true);
        itemFromService = itemService.saveNewItem(userDtoSaved1.getId(), itemDto);
        itemDto.setName("Name2");
        itemDto.setDescription("Description2");
        itemFromService2 = itemService.saveNewItem(userDtoSaved1.getId(), itemDto);
    }

    @Test
    void saveNewBookingTest() {
        BookingRequestDto bookingRequestDto = new BookingRequestDto();
        bookingRequestDto.setItemId(itemFromService.getId());
        bookingRequestDto.setStart(LocalDateTime.now().plusNanos(4000000));
        bookingRequestDto.setEnd(LocalDateTime.now().plusNanos(8000000));
        service.saveNewBooking(userDtoSaved2.getId(), bookingRequestDto);

        TypedQuery<Booking> query = em.createQuery("SELECT b from Booking b where b.item.id = :id",
                Booking.class);
        Booking booking = query
                .setParameter("id", itemFromService.getId())
                .getSingleResult();

        assertThat(booking.getId(), notNullValue());
        assertThat(booking.getBooker().getId(), equalTo(userDtoSaved2.getId()));
        assertThat(booking.getItem().getId(), equalTo(itemFromService.getId()));
    }

    @Test
    void saveNewBookingWithWrongUserTest() {
        BookingRequestDto bookingRequestDto = new BookingRequestDto();
        bookingRequestDto.setItemId(itemFromService.getId());
        bookingRequestDto.setStart(LocalDateTime.now().plusNanos(4000000));
        bookingRequestDto.setEnd(LocalDateTime.now().plusNanos(8000000));

        try {
            service.saveNewBooking(userDtoSaved1.getId(), bookingRequestDto);
        } catch (NotFoundException e) {
            assertThat(e.getMessage(), equalTo("The user cannot rent his items"));
        }
    }

    @Test
    void saveNewBookingWithUnavailableItemTest() {
        BookingRequestDto bookingRequestDto = new BookingRequestDto();
        itemFromService.setAvailable(false);
        itemService.updateItem(userDtoSaved1.getId(), itemFromService.getId(), itemFromService);
        bookingRequestDto.setItemId(itemFromService.getId());
        bookingRequestDto.setStart(LocalDateTime.now().plusNanos(4000000));
        bookingRequestDto.setEnd(LocalDateTime.now().plusNanos(8000000));

        try {
            service.saveNewBooking(userDtoSaved2.getId(), bookingRequestDto);
        } catch (ValidationException e) {
            assertThat(e.getMessage(), equalTo("This item isn't available"));
        }
    }

    @Test
    void saveNewBookingWithoutStartItemTest() {
        BookingRequestDto bookingRequestDto = new BookingRequestDto();
        itemFromService.setAvailable(false);
        itemService.updateItem(userDtoSaved1.getId(), itemFromService.getId(), itemFromService);
        bookingRequestDto.setItemId(itemFromService.getId());
        bookingRequestDto.setEnd(LocalDateTime.now().plusNanos(8000000));

        try {
            service.saveNewBooking(userDtoSaved2.getId(), bookingRequestDto);
        } catch (ValidationException e) {
            assertThat(e.getMessage(), equalTo("The booking start time field cannot be empty"));
        }
    }

    @Test
    void saveNewBookingWithoutEndItemTest() {
        BookingRequestDto bookingRequestDto = new BookingRequestDto();
        itemFromService.setAvailable(false);
        itemService.updateItem(userDtoSaved1.getId(), itemFromService.getId(), itemFromService);
        bookingRequestDto.setItemId(itemFromService.getId());
        bookingRequestDto.setStart(LocalDateTime.now().plusNanos(4000000));

        try {
            service.saveNewBooking(userDtoSaved2.getId(), bookingRequestDto);
        } catch (ValidationException e) {
            assertThat(e.getMessage(), equalTo("The booking end time field cannot be empty"));
        }
    }

    @Test
    void saveNewBookingWithWrongStartItemTest() {
        BookingRequestDto bookingRequestDto = new BookingRequestDto();
        itemFromService.setAvailable(false);
        itemService.updateItem(userDtoSaved1.getId(), itemFromService.getId(), itemFromService);
        bookingRequestDto.setItemId(itemFromService.getId());
        bookingRequestDto.setStart(LocalDateTime.now().minusSeconds(4));
        bookingRequestDto.setEnd(LocalDateTime.now().plusNanos(8000000));

        try {
            service.saveNewBooking(userDtoSaved2.getId(), bookingRequestDto);
        } catch (ValidationException e) {
            assertThat(e.getMessage(), equalTo("The booking start time can't be in past"));
        }
    }

    @Test
    void saveNewBookingWithWrongEndItemTest() {
        BookingRequestDto bookingRequestDto = new BookingRequestDto();
        itemFromService.setAvailable(false);
        itemService.updateItem(userDtoSaved1.getId(), itemFromService.getId(), itemFromService);
        bookingRequestDto.setItemId(itemFromService.getId());
        bookingRequestDto.setStart(LocalDateTime.now().plusSeconds(4));
        bookingRequestDto.setEnd(LocalDateTime.now().minusSeconds(8));

        try {
            service.saveNewBooking(userDtoSaved2.getId(), bookingRequestDto);
        } catch (ValidationException e) {
            assertThat(e.getMessage(), equalTo("The booking end time can't be in past"));
        }
    }

    @Test
    void saveNewBookingWithEndBeforeStartWrongEndItemTest() {
        BookingRequestDto bookingRequestDto = new BookingRequestDto();
        itemFromService.setAvailable(false);
        itemService.updateItem(userDtoSaved1.getId(), itemFromService.getId(), itemFromService);
        bookingRequestDto.setItemId(itemFromService.getId());
        bookingRequestDto.setStart(LocalDateTime.now().plusSeconds(4));
        bookingRequestDto.setEnd(LocalDateTime.now().plusSeconds(1));

        try {
            service.saveNewBooking(userDtoSaved2.getId(), bookingRequestDto);
        } catch (ValidationException e) {
            assertThat(e.getMessage(), equalTo("The booking end time must be after then start time"));
        }
    }

    @Test
    void saveNewBookingWithEndIsTheSameLikeStartWrongEndItemTest() {
        BookingRequestDto bookingRequestDto = new BookingRequestDto();
        itemFromService.setAvailable(false);
        itemService.updateItem(userDtoSaved1.getId(), itemFromService.getId(), itemFromService);
        bookingRequestDto.setItemId(itemFromService.getId());
        LocalDateTime time = LocalDateTime.now().plusSeconds(3);
        bookingRequestDto.setStart(time);
        bookingRequestDto.setEnd(time);

        try {
            service.saveNewBooking(userDtoSaved2.getId(), bookingRequestDto);
        } catch (ValidationException e) {
            assertThat(e.getMessage(), equalTo("The booking start time and end time can't be the same"));
        }
    }

    @Test
    void saveNewBookingWithWrongItemIdTest() {
        BookingRequestDto bookingRequestDto = new BookingRequestDto();
        itemFromService.setAvailable(false);
        itemService.updateItem(userDtoSaved1.getId(), itemFromService.getId(), itemFromService);
        bookingRequestDto.setItemId(-2);
        bookingRequestDto.setStart(LocalDateTime.now().plusSeconds(4));
        bookingRequestDto.setEnd(LocalDateTime.now().plusSeconds(5));

        try {
            service.saveNewBooking(userDtoSaved2.getId(), bookingRequestDto);
        } catch (ValidationException e) {
            assertThat(e.getMessage(), equalTo("The item id error"));
        }
    }

    @Test
    void bookingApprovedConformationTest() {
        BookingRequestDto bookingRequestDto = new BookingRequestDto();
        bookingRequestDto.setItemId(itemFromService.getId());
        bookingRequestDto.setStart(LocalDateTime.now().plusNanos(4000000));
        bookingRequestDto.setEnd(LocalDateTime.now().plusNanos(8000000));
        BookingResponseDto bookingResponseDto = service.saveNewBooking(userDtoSaved2.getId(), bookingRequestDto);
        service.bookingConfirmation(userDtoSaved1.getId(), bookingResponseDto.getId(), true);

        TypedQuery<Booking> query = em.createQuery("select b from Booking b where b.item.id = :id",
                Booking.class);
        Booking booking = query
                .setParameter("id", itemFromService.getId())
                .getSingleResult();

        assertThat(booking.getId(), notNullValue());
        assertThat(booking.getBooker().getId(), equalTo(userDtoSaved2.getId()));
        assertThat(booking.getItem().getId(), equalTo(itemFromService.getId()));
        assertThat(booking.getStatus(), equalTo(BookingStatus.APPROVED));
    }

    @Test
    void bookingRejectedConformationTest() {
        BookingRequestDto bookingRequestDto = new BookingRequestDto();
        bookingRequestDto.setItemId(itemFromService.getId());
        bookingRequestDto.setStart(LocalDateTime.now().plusNanos(4000000));
        bookingRequestDto.setEnd(LocalDateTime.now().plusNanos(8000000));
        BookingResponseDto bookingResponseDto = service.saveNewBooking(userDtoSaved2.getId(), bookingRequestDto);
        service.bookingConfirmation(userDtoSaved1.getId(), bookingResponseDto.getId(), false);

        TypedQuery<Booking> query = em.createQuery("SELECT b from Booking b where b.item.id = :id",
                Booking.class);
        Booking booking = query
                .setParameter("id", itemFromService.getId())
                .getSingleResult();

        assertThat(booking.getId(), notNullValue());
        assertThat(booking.getBooker().getId(), equalTo(userDtoSaved2.getId()));
        assertThat(booking.getItem().getId(), equalTo(itemFromService.getId()));
        assertThat(booking.getStatus(), equalTo(BookingStatus.REJECTED));
    }

    @Test
    void getBookingByWrongIdTest() {
        try {
            service.getBooking(100);
        } catch (NotFoundException e) {
            assertThat(e.getMessage(), equalTo("Booking id error"));
        }
    }

    @Test
    void getBookingByIdByBookerTest() {
        BookingRequestDto bookingRequestDto = new BookingRequestDto();
        bookingRequestDto.setItemId(itemFromService.getId());
        bookingRequestDto.setStart(LocalDateTime.now().plusNanos(4000000));
        bookingRequestDto.setEnd(LocalDateTime.now().plusNanos(8000000));
        BookingResponseDto bookingResponseDto = service.saveNewBooking(userDtoSaved2.getId(), bookingRequestDto);
        BookingResponseDto bookingResponseDtoGettingById = service.getBookingById(userDtoSaved2.getId(),
                bookingResponseDto.getId());

        TypedQuery<Booking> query = em.createQuery("SELECT b from Booking b where b.id = :id",
                Booking.class);
        Booking booking = query
                .setParameter("id", bookingResponseDtoGettingById.getId())
                .getSingleResult();

        assertThat(booking.getId(), notNullValue());
        assertThat(booking.getBooker().getId(), equalTo(bookingResponseDtoGettingById.getBooker().getId()));
        assertThat(booking.getItem().getId(), equalTo(bookingResponseDtoGettingById.getItem().getId()));
    }


    @Test
    void getBookingsByBookerIdWithCurrenStateTest() {
        Booking booking = new Booking();
        booking.setItem(ItemMapper.makeItem(itemFromService));
        booking.setBooker(UserMapper.makeUser(userDtoSaved2));
        booking.setStart(LocalDateTime.now().minusSeconds(30));
        booking.setEnd(LocalDateTime.now().plusSeconds(10));
        booking.setStatus(BookingStatus.APPROVED);
        Booking bookingFromRepository = repository.save(booking);

        List<BookingResponseDto> bookingResponseDtos = service.getBookingsByBookerId(userDtoSaved2.getId(),
                BookingState.CURRENT);

        assertThat(bookingResponseDtos.size(), equalTo(1));
        assertThat(bookingResponseDtos.get(0).getId(), equalTo(bookingFromRepository.getId()));
    }

    @Test
    void getBookingsByBookerIdWithPastStateTest() {
        Booking booking = new Booking();
        booking.setItem(ItemMapper.makeItem(itemFromService));
        booking.setBooker(UserMapper.makeUser(userDtoSaved2));
        booking.setStart(LocalDateTime.now().minusSeconds(30));
        booking.setEnd(LocalDateTime.now().minusSeconds(10));
        booking.setStatus(BookingStatus.APPROVED);
        Booking bookingFromRepository = repository.save(booking);

        List<BookingResponseDto> bookingResponseDtos = service.getBookingsByBookerId(userDtoSaved2.getId(),
                BookingState.PAST);

        assertThat(bookingResponseDtos.size(), equalTo(1));
        assertThat(bookingResponseDtos.get(0).getId(), equalTo(bookingFromRepository.getId()));
    }

    @Test
    void getBookingsByBookerIdWithFutureStateTest() {
        Booking booking = new Booking();
        booking.setItem(ItemMapper.makeItem(itemFromService));
        booking.setBooker(UserMapper.makeUser(userDtoSaved2));
        booking.setStart(LocalDateTime.now().plusSeconds(100));
        booking.setEnd(LocalDateTime.now().plusSeconds(120));
        booking.setStatus(BookingStatus.APPROVED);
        Booking bookingFromRepository = repository.save(booking);

        List<BookingResponseDto> bookingResponseDtos = service.getBookingsByBookerId(userDtoSaved2.getId(),
                BookingState.FUTURE);

        assertThat(bookingResponseDtos.size(), equalTo(1));
        assertThat(bookingResponseDtos.get(0).getId(), equalTo(bookingFromRepository.getId()));
    }

    @Test
    void getBookingsByBookerIdWithRejectedStateTest() {
        Booking booking = new Booking();
        booking.setItem(ItemMapper.makeItem(itemFromService));
        booking.setBooker(UserMapper.makeUser(userDtoSaved2));
        booking.setStart(LocalDateTime.now().minusSeconds(100));
        booking.setEnd(LocalDateTime.now().minusSeconds(80));
        booking.setStatus(BookingStatus.REJECTED);
        Booking bookingFromRepository = repository.save(booking);

        List<BookingResponseDto> bookingResponseDtos = service.getBookingsByBookerId(userDtoSaved2.getId(),
                BookingState.REJECTED);

        assertThat(bookingResponseDtos.size(), equalTo(1));
        assertThat(bookingResponseDtos.get(0).getId(), equalTo(bookingFromRepository.getId()));
    }

    @Test
    void getBookingsByBookerIdWithWaitingStateTest() {
        Booking booking = new Booking();
        booking.setItem(ItemMapper.makeItem(itemFromService));
        booking.setBooker(UserMapper.makeUser(userDtoSaved2));
        booking.setStart(LocalDateTime.now().minusSeconds(100));
        booking.setEnd(LocalDateTime.now().minusSeconds(80));
        booking.setStatus(BookingStatus.WAITING);
        Booking bookingFromRepository = repository.save(booking);

        List<BookingResponseDto> bookingResponseDtos = service.getBookingsByBookerId(userDtoSaved2.getId(),
                BookingState.WAITING);

        assertThat(bookingResponseDtos.size(), equalTo(1));
        assertThat(bookingResponseDtos.get(0).getId(), equalTo(bookingFromRepository.getId()));
    }

    @Test
    void getBookingsByBookerIdWithoutStateTest() {
        Booking booking = new Booking();
        booking.setItem(ItemMapper.makeItem(itemFromService));
        booking.setBooker(UserMapper.makeUser(userDtoSaved2));
        booking.setStart(LocalDateTime.now().minusSeconds(100));
        booking.setEnd(LocalDateTime.now().minusSeconds(80));
        booking.setStatus(BookingStatus.WAITING);
        Booking bookingFromRepository = repository.save(booking);

        List<BookingResponseDto> bookingResponseDtos = service.getBookingsByBookerId(userDtoSaved2.getId(),
                null);

        assertThat(bookingResponseDtos.size(), equalTo(1));
        assertThat(bookingResponseDtos.get(0).getId(), equalTo(bookingFromRepository.getId()));
    }

    @Test
    void getBookingsByBookerIdWithCurrenStateWithPaginationTest() {
        Booking booking1 = new Booking();
        booking1.setItem(ItemMapper.makeItem(itemFromService));
        booking1.setBooker(UserMapper.makeUser(userDtoSaved2));
        booking1.setStart(LocalDateTime.now().minusSeconds(30));
        booking1.setEnd(LocalDateTime.now().plusSeconds(10));
        booking1.setStatus(BookingStatus.APPROVED);
        repository.save(booking1);
        Booking booking2 = new Booking();
        booking2.setItem(ItemMapper.makeItem(itemFromService2));
        booking2.setBooker(UserMapper.makeUser(userDtoSaved2));
        booking2.setStart(LocalDateTime.now().minusSeconds(30));
        booking2.setEnd(LocalDateTime.now().plusSeconds(10));
        booking2.setStatus(BookingStatus.APPROVED);
        repository.save(booking2);

        List<BookingResponseDto> bookingResponseDtos = service.getBookingsByBookerId(userDtoSaved2.getId(),
                BookingState.CURRENT, 1, 1);

        assertThat(bookingResponseDtos.size(), equalTo(1));
    }

    @Test
    void getBookingsByBookerIdWithPastStateWithPaginationTest() {
        Booking booking1 = new Booking();
        booking1.setItem(ItemMapper.makeItem(itemFromService));
        booking1.setBooker(UserMapper.makeUser(userDtoSaved2));
        booking1.setStart(LocalDateTime.now().minusSeconds(100));
        booking1.setEnd(LocalDateTime.now().minusSeconds(50));
        booking1.setStatus(BookingStatus.APPROVED);
        repository.save(booking1);
        Booking booking2 = new Booking();
        booking2.setItem(ItemMapper.makeItem(itemFromService2));
        booking2.setBooker(UserMapper.makeUser(userDtoSaved2));
        booking2.setStart(LocalDateTime.now().minusSeconds(100));
        booking2.setEnd(LocalDateTime.now().minusSeconds(50));
        booking2.setStatus(BookingStatus.APPROVED);
        repository.save(booking2);

        List<BookingResponseDto> bookingResponseDtos = service.getBookingsByBookerId(userDtoSaved2.getId(),
                BookingState.PAST, 1, 1);

        assertThat(bookingResponseDtos.size(), equalTo(1));
    }

    @Test
    void getBookingsByBookerIdWithFutureStateWithPaginationTest() {
        Booking booking1 = new Booking();
        booking1.setItem(ItemMapper.makeItem(itemFromService));
        booking1.setBooker(UserMapper.makeUser(userDtoSaved2));
        booking1.setStart(LocalDateTime.now().plusSeconds(100));
        booking1.setEnd(LocalDateTime.now().plusSeconds(120));
        booking1.setStatus(BookingStatus.APPROVED);
        repository.save(booking1);
        Booking booking2 = new Booking();
        booking2.setItem(ItemMapper.makeItem(itemFromService2));
        booking2.setBooker(UserMapper.makeUser(userDtoSaved2));
        booking2.setStart(LocalDateTime.now().plusSeconds(100));
        booking2.setEnd(LocalDateTime.now().plusSeconds(120));
        booking2.setStatus(BookingStatus.APPROVED);
        repository.save(booking2);

        List<BookingResponseDto> bookingResponseDtos = service.getBookingsByBookerId(userDtoSaved2.getId(),
                BookingState.FUTURE, 1, 1);

        assertThat(bookingResponseDtos.size(), equalTo(1));
    }

    @Test
    void getBookingsByBookerIdWithRejectedStateWithPaginationTest() {
        Booking booking1 = new Booking();
        booking1.setItem(ItemMapper.makeItem(itemFromService));
        booking1.setBooker(UserMapper.makeUser(userDtoSaved2));
        booking1.setStart(LocalDateTime.now().plusSeconds(100));
        booking1.setEnd(LocalDateTime.now().plusSeconds(120));
        booking1.setStatus(BookingStatus.REJECTED);
        repository.save(booking1);
        Booking booking2 = new Booking();
        booking2.setItem(ItemMapper.makeItem(itemFromService2));
        booking2.setBooker(UserMapper.makeUser(userDtoSaved2));
        booking2.setStart(LocalDateTime.now().plusSeconds(100));
        booking2.setEnd(LocalDateTime.now().plusSeconds(120));
        booking2.setStatus(BookingStatus.REJECTED);
        repository.save(booking2);

        List<BookingResponseDto> bookingResponseDtos1 = service.getBookingsByBookerId(userDtoSaved2.getId(),
                BookingState.REJECTED, 1, 1);

        assertThat(bookingResponseDtos1.size(), equalTo(1));
    }

    @Test
    void getBookingsByBookerIdWithWaitingStateWithPaginationTest() {
        Booking booking1 = new Booking();
        booking1.setItem(ItemMapper.makeItem(itemFromService));
        booking1.setBooker(UserMapper.makeUser(userDtoSaved2));
        booking1.setStart(LocalDateTime.now().plusSeconds(100));
        booking1.setEnd(LocalDateTime.now().plusSeconds(120));
        booking1.setStatus(BookingStatus.WAITING);
        repository.save(booking1);
        Booking booking2 = new Booking();
        booking2.setItem(ItemMapper.makeItem(itemFromService2));
        booking2.setBooker(UserMapper.makeUser(userDtoSaved2));
        booking2.setStart(LocalDateTime.now().plusSeconds(100));
        booking2.setEnd(LocalDateTime.now().plusSeconds(120));
        booking2.setStatus(BookingStatus.WAITING);
        repository.save(booking2);

        List<BookingResponseDto> bookingResponseDtos = service.getBookingsByBookerId(userDtoSaved2.getId(),
                BookingState.WAITING, 1, 1);

        assertThat(bookingResponseDtos.size(), equalTo(1));
    }

    @Test
    void getBookingsByBookerIdWithoutStateWithPaginationTest() {
        Booking booking1 = new Booking();
        booking1.setItem(ItemMapper.makeItem(itemFromService));
        booking1.setBooker(UserMapper.makeUser(userDtoSaved2));
        booking1.setStart(LocalDateTime.now().plusSeconds(100));
        booking1.setEnd(LocalDateTime.now().plusSeconds(120));
        booking1.setStatus(BookingStatus.WAITING);
        repository.save(booking1);
        Booking booking2 = new Booking();
        booking2.setItem(ItemMapper.makeItem(itemFromService2));
        booking2.setBooker(UserMapper.makeUser(userDtoSaved2));
        booking2.setStart(LocalDateTime.now().plusSeconds(100));
        booking2.setEnd(LocalDateTime.now().plusSeconds(120));
        booking2.setStatus(BookingStatus.WAITING);
        repository.save(booking2);

        List<BookingResponseDto> bookingResponseDtos = service.getBookingsByBookerId(userDtoSaved2.getId(),
                null, 1, 1);

        assertThat(bookingResponseDtos.size(), equalTo(1));
    }

    @Test
    void getBookingsByBookerIdWithoutStateWithWrongPaginationParametersTest() {
        Booking booking1 = new Booking();
        booking1.setItem(ItemMapper.makeItem(itemFromService));
        booking1.setBooker(UserMapper.makeUser(userDtoSaved2));
        booking1.setStart(LocalDateTime.now().plusSeconds(100));
        booking1.setEnd(LocalDateTime.now().plusSeconds(120));
        booking1.setStatus(BookingStatus.WAITING);
        repository.save(booking1);
        Booking booking2 = new Booking();
        booking2.setItem(ItemMapper.makeItem(itemFromService2));
        booking2.setBooker(UserMapper.makeUser(userDtoSaved2));
        booking2.setStart(LocalDateTime.now().plusSeconds(100));
        booking2.setEnd(LocalDateTime.now().plusSeconds(120));
        booking2.setStatus(BookingStatus.WAITING);
        repository.save(booking2);

        try {
            service.getBookingsByBookerId(userDtoSaved2.getId(),
                    null, -1, 1);
        } catch (ValidationException e) {
            assertThat(e.getMessage(), equalTo("The from parameter can't be negative number"));
        }

        try {
            service.getBookingsByBookerId(userDtoSaved2.getId(),
                    null, 1, 0);
        } catch (ValidationException e) {
            assertThat(e.getMessage(), equalTo("The size parameter must be positive number"));
        }
    }

    @Test
    void getBookingsForAllItemsByOwnerIdWithCurrenStateTest() {
        Booking booking = new Booking();
        booking.setItem(ItemMapper.makeItem(itemFromService));
        booking.setBooker(UserMapper.makeUser(userDtoSaved2));
        booking.setStart(LocalDateTime.now().minusSeconds(20));
        booking.setEnd(LocalDateTime.now().plusSeconds(5));
        booking.setStatus(BookingStatus.APPROVED);
        Booking bookingFromRepository = repository.save(booking);

        List<BookingResponseDto> bookingResponseDtos = service.getBookingsForAllItemsByOwnerId(userDtoSaved1.getId(),
                BookingState.CURRENT);

        assertThat(bookingResponseDtos.size(), equalTo(1));
        assertThat(bookingResponseDtos.get(0).getId(), equalTo(bookingFromRepository.getId()));
    }

    @Test
    void getBookingsForAllItemsByOwnerIdWithPastStateTest() {
        Booking booking = new Booking();
        booking.setItem(ItemMapper.makeItem(itemFromService));
        booking.setBooker(UserMapper.makeUser(userDtoSaved2));
        booking.setStart(LocalDateTime.now().minusSeconds(20));
        booking.setEnd(LocalDateTime.now().minusSeconds(5));
        booking.setStatus(BookingStatus.APPROVED);
        Booking bookingFromRepository = repository.save(booking);

        List<BookingResponseDto> bookingResponseDtos = service.getBookingsForAllItemsByOwnerId(userDtoSaved1.getId(),
                BookingState.PAST);

        assertThat(bookingResponseDtos.size(), equalTo(1));
        assertThat(bookingResponseDtos.get(0).getId(), equalTo(bookingFromRepository.getId()));
    }

    @Test
    void getBookingsForAllItemsByOwnerIdWithFutureStateTest() {
        Booking booking = new Booking();
        booking.setItem(ItemMapper.makeItem(itemFromService));
        booking.setBooker(UserMapper.makeUser(userDtoSaved2));
        booking.setStart(LocalDateTime.now().plusSeconds(20));
        booking.setEnd(LocalDateTime.now().plusSeconds(30));
        booking.setStatus(BookingStatus.APPROVED);
        Booking bookingFromRepository = repository.save(booking);

        List<BookingResponseDto> bookingResponseDtos = service.getBookingsForAllItemsByOwnerId(userDtoSaved1.getId(),
                BookingState.FUTURE);

        assertThat(bookingResponseDtos.size(), equalTo(1));
        assertThat(bookingResponseDtos.get(0).getId(), equalTo(bookingFromRepository.getId()));
    }

    @Test
    void getBookingsForAllItemsByOwnerIdWithRejectedStateTest() {
        Booking booking = new Booking();
        booking.setItem(ItemMapper.makeItem(itemFromService));
        booking.setBooker(UserMapper.makeUser(userDtoSaved2));
        booking.setStart(LocalDateTime.now().plusSeconds(20));
        booking.setEnd(LocalDateTime.now().plusSeconds(30));
        booking.setStatus(BookingStatus.REJECTED);
        Booking bookingFromRepository = repository.save(booking);

        List<BookingResponseDto> bookingResponseDtos = service.getBookingsForAllItemsByOwnerId(userDtoSaved1.getId(),
                BookingState.REJECTED);

        assertThat(bookingResponseDtos.size(), equalTo(1));
        assertThat(bookingResponseDtos.get(0).getId(), equalTo(bookingFromRepository.getId()));
    }

    @Test
    void getBookingsForAllItemsByOwnerIdWithWaitingStateTest() {
        Booking booking = new Booking();
        booking.setItem(ItemMapper.makeItem(itemFromService));
        booking.setBooker(UserMapper.makeUser(userDtoSaved2));
        booking.setStart(LocalDateTime.now().plusSeconds(20));
        booking.setEnd(LocalDateTime.now().plusSeconds(30));
        booking.setStatus(BookingStatus.WAITING);
        Booking bookingFromRepository = repository.save(booking);

        List<BookingResponseDto> bookingResponseDtos = service.getBookingsForAllItemsByOwnerId(userDtoSaved1.getId(),
                BookingState.WAITING);

        assertThat(bookingResponseDtos.size(), equalTo(1));
        assertThat(bookingResponseDtos.get(0).getId(), equalTo(bookingFromRepository.getId()));
    }

    @Test
    void getBookingsForAllItemsByOwnerIdWithoutStateTest() {
        Booking booking = new Booking();
        booking.setItem(ItemMapper.makeItem(itemFromService));
        booking.setBooker(UserMapper.makeUser(userDtoSaved2));
        booking.setStart(LocalDateTime.now().plusSeconds(20));
        booking.setEnd(LocalDateTime.now().plusSeconds(30));
        booking.setStatus(BookingStatus.WAITING);
        Booking bookingFromRepository = repository.save(booking);

        List<BookingResponseDto> bookingResponseDtos = service.getBookingsForAllItemsByOwnerId(userDtoSaved1.getId(),
                null);

        assertThat(bookingResponseDtos.size(), equalTo(1));
        assertThat(bookingResponseDtos.get(0).getId(), equalTo(bookingFromRepository.getId()));
    }

    @Test
    void getBookingsForAllItemsByOwnerIdWithCurrenStateWithPaginationTest() {
        Booking booking1 = new Booking();
        booking1.setItem(ItemMapper.makeItem(itemFromService));
        booking1.setBooker(UserMapper.makeUser(userDtoSaved2));
        booking1.setStart(LocalDateTime.now().minusSeconds(30));
        booking1.setEnd(LocalDateTime.now().plusSeconds(10));
        booking1.setStatus(BookingStatus.APPROVED);
        repository.save(booking1);
        Booking booking2 = new Booking();
        booking2.setItem(ItemMapper.makeItem(itemFromService2));
        booking2.setBooker(UserMapper.makeUser(userDtoSaved2));
        booking2.setStart(LocalDateTime.now().minusSeconds(30));
        booking2.setEnd(LocalDateTime.now().plusSeconds(10));
        booking2.setStatus(BookingStatus.APPROVED);
        repository.save(booking2);

        List<BookingResponseDto> bookingResponseDtos = service.getBookingsForAllItemsByOwnerId(userDtoSaved1.getId(),
                BookingState.CURRENT, 1, 1);

        assertThat(bookingResponseDtos.size(), equalTo(1));
    }

    @Test
    void getBookingsForAllItemsByOwnerIdWithPastStateWithPaginationTest() {
        Booking booking1 = new Booking();
        booking1.setItem(ItemMapper.makeItem(itemFromService));
        booking1.setBooker(UserMapper.makeUser(userDtoSaved2));
        booking1.setStart(LocalDateTime.now().minusSeconds(100));
        booking1.setEnd(LocalDateTime.now().minusSeconds(50));
        booking1.setStatus(BookingStatus.APPROVED);
        repository.save(booking1);
        Booking booking2 = new Booking();
        booking2.setItem(ItemMapper.makeItem(itemFromService2));
        booking2.setBooker(UserMapper.makeUser(userDtoSaved2));
        booking2.setStart(LocalDateTime.now().minusSeconds(100));
        booking2.setEnd(LocalDateTime.now().minusSeconds(50));
        booking2.setStatus(BookingStatus.APPROVED);
        repository.save(booking2);

        List<BookingResponseDto> bookingResponseDtos = service.getBookingsForAllItemsByOwnerId(userDtoSaved1.getId(),
                BookingState.PAST, 1, 1);

        assertThat(bookingResponseDtos.size(), equalTo(1));
    }

    @Test
    void getBookingsForAllItemsByOwnerIdWithFutureStateWithPaginationTest() {
        Booking booking1 = new Booking();
        booking1.setItem(ItemMapper.makeItem(itemFromService));
        booking1.setBooker(UserMapper.makeUser(userDtoSaved2));
        booking1.setStart(LocalDateTime.now().plusSeconds(100));
        booking1.setEnd(LocalDateTime.now().plusSeconds(120));
        booking1.setStatus(BookingStatus.APPROVED);
        repository.save(booking1);
        Booking booking2 = new Booking();
        booking2.setItem(ItemMapper.makeItem(itemFromService2));
        booking2.setBooker(UserMapper.makeUser(userDtoSaved2));
        booking2.setStart(LocalDateTime.now().plusSeconds(100));
        booking2.setEnd(LocalDateTime.now().plusSeconds(120));
        booking2.setStatus(BookingStatus.APPROVED);
        repository.save(booking2);

        List<BookingResponseDto> bookingResponseDtos = service.getBookingsForAllItemsByOwnerId(userDtoSaved1.getId(),
                BookingState.FUTURE, 1, 1);

        assertThat(bookingResponseDtos.size(), equalTo(1));
    }

    @Test
    void getBookingsForAllItemsByOwnerIdWithRejectedStateWithPaginationTest() {
        Booking booking1 = new Booking();
        booking1.setItem(ItemMapper.makeItem(itemFromService));
        booking1.setBooker(UserMapper.makeUser(userDtoSaved2));
        booking1.setStart(LocalDateTime.now().plusSeconds(100));
        booking1.setEnd(LocalDateTime.now().plusSeconds(120));
        booking1.setStatus(BookingStatus.REJECTED);
        repository.save(booking1);
        Booking booking2 = new Booking();
        booking2.setItem(ItemMapper.makeItem(itemFromService2));
        booking2.setBooker(UserMapper.makeUser(userDtoSaved2));
        booking2.setStart(LocalDateTime.now().plusSeconds(100));
        booking2.setEnd(LocalDateTime.now().plusSeconds(120));
        booking2.setStatus(BookingStatus.REJECTED);
        repository.save(booking2);

        List<BookingResponseDto> bookingResponseDtos = service.getBookingsForAllItemsByOwnerId(userDtoSaved1.getId(),
                BookingState.REJECTED, 1, 1);

        assertThat(bookingResponseDtos.size(), equalTo(1));
    }

    @Test
    void getBookingsForAllItemsByOwnerIdWithWaitingStateWithPaginationTest() {
        Booking booking1 = new Booking();
        booking1.setItem(ItemMapper.makeItem(itemFromService));
        booking1.setBooker(UserMapper.makeUser(userDtoSaved2));
        booking1.setStart(LocalDateTime.now().plusSeconds(100));
        booking1.setEnd(LocalDateTime.now().plusSeconds(120));
        booking1.setStatus(BookingStatus.WAITING);
        repository.save(booking1);
        Booking booking2 = new Booking();
        booking2.setItem(ItemMapper.makeItem(itemFromService2));
        booking2.setBooker(UserMapper.makeUser(userDtoSaved2));
        booking2.setStart(LocalDateTime.now().plusSeconds(100));
        booking2.setEnd(LocalDateTime.now().plusSeconds(120));
        booking2.setStatus(BookingStatus.WAITING);
        repository.save(booking2);

        List<BookingResponseDto> bookingResponseDtos = service.getBookingsForAllItemsByOwnerId(userDtoSaved1.getId(),
                BookingState.WAITING, 1, 1);

        assertThat(bookingResponseDtos.size(), equalTo(1));
    }

    @Test
    void getBookingsForAllItemsByOwnerIdWithoutStateWithPaginationTest() {
        Booking booking1 = new Booking();
        booking1.setItem(ItemMapper.makeItem(itemFromService));
        booking1.setBooker(UserMapper.makeUser(userDtoSaved2));
        booking1.setStart(LocalDateTime.now().plusSeconds(100));
        booking1.setEnd(LocalDateTime.now().plusSeconds(120));
        booking1.setStatus(BookingStatus.WAITING);
        repository.save(booking1);
        Booking booking2 = new Booking();
        booking2.setItem(ItemMapper.makeItem(itemFromService2));
        booking2.setBooker(UserMapper.makeUser(userDtoSaved2));
        booking2.setStart(LocalDateTime.now().plusSeconds(100));
        booking2.setEnd(LocalDateTime.now().plusSeconds(120));
        booking2.setStatus(BookingStatus.WAITING);
        repository.save(booking2);


        List<BookingResponseDto> bookingResponseDtos = service.getBookingsForAllItemsByOwnerId(userDtoSaved1.getId(),
                null, 1, 1);

        assertThat(bookingResponseDtos.size(), equalTo(1));
    }
}
