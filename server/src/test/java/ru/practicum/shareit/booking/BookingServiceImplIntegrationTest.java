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
        bookingRequestDto.setStart(LocalDateTime.now().plusSeconds(40));
        bookingRequestDto.setEnd(LocalDateTime.now().plusSeconds(80));
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
            assertThat(e.getMessage(), equalTo("This item already belongs to you, " +
                    "so you can't rent it"));
        }
    }

    @Test
    void bookingApprovedConformationTest() {
        Booking booking = new Booking();
        booking.setItem(ItemMapper.makeItem(itemFromService));
        booking.setBooker(UserMapper.makeUser(userDtoSaved2));
        booking.setStart(LocalDateTime.now().plusSeconds(10));
        booking.setEnd(LocalDateTime.now().plusSeconds(15));
        booking.setStatus(BookingStatus.WAITING);
        Booking bookingFromRepository = repository.save(booking);
        service.bookingConfirmation(userDtoSaved1.getId(), bookingFromRepository.getId(), true);

        TypedQuery<Booking> query = em.createQuery("select b from Booking b where b.item.id = :id",
                Booking.class);
        Booking bookingFromEm = query
                .setParameter("id", itemFromService.getId())
                .getSingleResult();

        assertThat(bookingFromEm.getId(), notNullValue());
        assertThat(bookingFromEm.getBooker().getId(), equalTo(userDtoSaved2.getId()));
        assertThat(bookingFromEm.getItem().getId(), equalTo(itemFromService.getId()));
        assertThat(bookingFromEm.getStatus(), equalTo(BookingStatus.APPROVED));
    }

    @Test
    void bookingRejectedConformationTest() {
        Booking booking = new Booking();
        booking.setItem(ItemMapper.makeItem(itemFromService));
        booking.setBooker(UserMapper.makeUser(userDtoSaved2));
        booking.setStart(LocalDateTime.now().plusSeconds(10));
        booking.setEnd(LocalDateTime.now().plusSeconds(15));
        booking.setStatus(BookingStatus.WAITING);
        Booking bookingFromRepository = repository.save(booking);
        service.bookingConfirmation(userDtoSaved1.getId(), bookingFromRepository.getId(), false);

        TypedQuery<Booking> query = em.createQuery("SELECT b from Booking b where b.item.id = :id",
                Booking.class);
        Booking bookingFromEm = query
                .setParameter("id", itemFromService.getId())
                .getSingleResult();

        assertThat(bookingFromEm.getId(), notNullValue());
        assertThat(bookingFromEm.getBooker().getId(), equalTo(userDtoSaved2.getId()));
        assertThat(bookingFromEm.getItem().getId(), equalTo(itemFromService.getId()));
        assertThat(bookingFromEm.getStatus(), equalTo(BookingStatus.REJECTED));
    }

    @Test
    void bookingAlreadyApprovedConformationTest() {
        Booking booking = new Booking();
        booking.setItem(ItemMapper.makeItem(itemFromService));
        booking.setBooker(UserMapper.makeUser(userDtoSaved2));
        booking.setStart(LocalDateTime.now().plusSeconds(10));
        booking.setEnd(LocalDateTime.now().plusSeconds(15));
        booking.setStatus(BookingStatus.WAITING);
        Booking bookingFromRepository = repository.save(booking);
        service.bookingConfirmation(userDtoSaved1.getId(), bookingFromRepository.getId(), true);

        try {
            service.bookingConfirmation(userDtoSaved1.getId(), bookingFromRepository.getId(), true);
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), equalTo("Approved error"));
        }
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
    void getBookingById() {
        Booking newBooking = new Booking();
        newBooking.setItem(ItemMapper.makeItem(itemFromService));
        newBooking.setStart(LocalDateTime.now().minusSeconds(10));
        newBooking.setEnd(LocalDateTime.now().minusSeconds(5));
        newBooking.setBooker(UserMapper.makeUser(userDtoSaved2));
        newBooking.setStatus(BookingStatus.APPROVED);
        Booking bookingFromRepository = repository.save(newBooking);
        BookingResponseDto bookingResponseDto = service.getBookingById(userDtoSaved1.getId(),
                bookingFromRepository.getId());

        TypedQuery<Booking> query = em.createQuery("SELECT b from Booking b where b.id = :id",
                Booking.class);
        Booking booking = query
                .setParameter("id", bookingFromRepository.getId())
                .getSingleResult();

        assertThat(booking.getId(), notNullValue());
        assertThat(booking.getBooker().getId(), equalTo(bookingResponseDto.getBooker().getId()));
        assertThat(booking.getItem().getId(), equalTo(bookingResponseDto.getItem().getId()));
    }

    @Test
    void getBookingByIdByBookerTest() {
        Booking newBooking = new Booking();
        newBooking.setItem(ItemMapper.makeItem(itemFromService));
        newBooking.setStart(LocalDateTime.now().minusSeconds(10));
        newBooking.setEnd(LocalDateTime.now().minusSeconds(5));
        newBooking.setBooker(UserMapper.makeUser(userDtoSaved2));
        newBooking.setStatus(BookingStatus.APPROVED);
        Booking bookingFromRepository = repository.save(newBooking);

        TypedQuery<Booking> query = em.createQuery("SELECT b from Booking b where b.id = :id",
                Booking.class);
        Booking booking = query
                .setParameter("id", bookingFromRepository.getId())
                .getSingleResult();

        assertThat(booking.getId(), notNullValue());
        assertThat(booking.getBooker().getId(), equalTo(newBooking.getBooker().getId()));
        assertThat(booking.getItem().getId(), equalTo(newBooking.getItem().getId()));
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
                "CURRENT", 1, 1);

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
                "PAST", 1, 1);

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
                "FUTURE", 1, 1);

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
                "REJECTED", 1, 1);

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
                "WAITING", 1, 1);

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
