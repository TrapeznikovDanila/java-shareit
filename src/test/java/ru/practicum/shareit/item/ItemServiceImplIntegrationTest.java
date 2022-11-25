package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentsDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemForRequestDto;
import ru.practicum.shareit.item.model.Comments;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestServiceImpl;
import ru.practicum.shareit.request.dto.ItemRequestDto;
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
public class ItemServiceImplIntegrationTest {

    private final EntityManager em;

    private final ItemServiceImpl service;

    private final UserServiceImpl userService;

    private final ItemRequestServiceImpl itemRequestService;

    private final BookingRepository bookingRepository;

    @Test
    void saveNewItemTest() {
        UserDto userDtoNotSaved = new UserDto();
        userDtoNotSaved.setName("Name");
        userDtoNotSaved.setEmail("e@mail.ru");
        UserDto userDtoSaved = userService.saveNewUser(userDtoNotSaved);
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Name");
        itemDto.setDescription("Description");
        itemDto.setAvailable(true);
        service.saveNewItem(userDtoSaved.getId(), itemDto);

        TypedQuery<Item> query = em.createQuery("SELECT i from Item i where i.name = :name",
                Item.class);
        Item item = query
                .setParameter("name", itemDto.getName())
                .getSingleResult();

        assertThat(item.getId(), notNullValue());
        assertThat(item.getName(), equalTo(itemDto.getName()));
        assertThat(item.getDescription(), equalTo(itemDto.getDescription()));
        assertThat(item.getAvailable(), equalTo(itemDto.getAvailable()));
    }

    @Test
    void saveNewItemWithoutAvailableTest() {
        UserDto userDtoNotSaved = new UserDto();
        userDtoNotSaved.setName("Name");
        userDtoNotSaved.setEmail("e@mail.ru");
        UserDto userDtoSaved = userService.saveNewUser(userDtoNotSaved);
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Name");
        itemDto.setDescription("Description");

        try {
            service.saveNewItem(userDtoSaved.getId(), itemDto);
        } catch (ValidationException e) {
            assertThat(e.getMessage(), equalTo("The availability field cannot be empty"));
        }
    }

    @Test
    void saveNewItemWithoutNameTest() {
        UserDto userDtoNotSaved = new UserDto();
        userDtoNotSaved.setName("Name");
        userDtoNotSaved.setEmail("e@mail.ru");
        UserDto userDtoSaved = userService.saveNewUser(userDtoNotSaved);
        ItemDto itemDto = new ItemDto();
        itemDto.setAvailable(true);
        itemDto.setDescription("Description");

        try {
            service.saveNewItem(userDtoSaved.getId(), itemDto);
        } catch (ValidationException e) {
            assertThat(e.getMessage(), equalTo("The name field cannot be empty"));
        }
    }

    @Test
    void saveNewItemWithoutDescriptionTest() {
        UserDto userDtoNotSaved = new UserDto();
        userDtoNotSaved.setName("Name");
        userDtoNotSaved.setEmail("e@mail.ru");
        UserDto userDtoSaved = userService.saveNewUser(userDtoNotSaved);
        ItemDto itemDto = new ItemDto();
        itemDto.setAvailable(true);
        itemDto.setName("Name");

        try {
            service.saveNewItem(userDtoSaved.getId(), itemDto);
        } catch (ValidationException e) {
            assertThat(e.getMessage(), equalTo("The description field cannot be empty"));
        }
    }

    @Test
    void updateItemTest() {
        UserDto userDtoNotSaved = new UserDto();
        userDtoNotSaved.setName("Name");
        userDtoNotSaved.setEmail("e@mail.ru");
        UserDto userDtoSaved = userService.saveNewUser(userDtoNotSaved);
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Name");
        itemDto.setDescription("Description");
        itemDto.setAvailable(true);
        ItemDto itemDtoSaved = service.saveNewItem(userDtoSaved.getId(), itemDto);
        itemDto.setName("NewName");
        itemDto.setDescription("NewDescription");
        itemDto.setAvailable(false);
        service.updateItem(userDtoSaved.getId(), itemDtoSaved.getId(), itemDto);


        TypedQuery<Item> query = em.createQuery("SELECT i from Item i where i.id = :id",
                Item.class);
        Item item = query
                .setParameter("id", itemDtoSaved.getId())
                .getSingleResult();

        assertThat(item.getId(), notNullValue());
        assertThat(item.getName(), equalTo(itemDto.getName()));
        assertThat(item.getDescription(), equalTo(itemDto.getDescription()));
    }

    @Test
    void updateWrongItemTest() {
        UserDto userDtoNotSaved = new UserDto();
        userDtoNotSaved.setName("Name");
        userDtoNotSaved.setEmail("e@mail.ru");
        UserDto userDtoSaved = userService.saveNewUser(userDtoNotSaved);
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Name");
        itemDto.setDescription("Description");
        itemDto.setAvailable(true);
        ItemDto itemDtoSaved = service.saveNewItem(userDtoSaved.getId(), itemDto);
        itemDto.setName("NewName");
        itemDto.setDescription("NewDescription");
        itemDto.setAvailable(false);

        try {
            service.updateItem(userDtoSaved.getId(), 15, itemDto);
        } catch (NotFoundException e) {
            assertThat(e.getMessage(), equalTo("Unknown item id"));
        }
    }

    @Test
    void updateItemByNotOwnerIdTest() {
        UserDto userDtoNotSaved = new UserDto();
        userDtoNotSaved.setName("Name");
        userDtoNotSaved.setEmail("e@mail.ru");
        UserDto userDtoSaved = userService.saveNewUser(userDtoNotSaved);
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Name");
        itemDto.setDescription("Description");
        itemDto.setAvailable(true);
        ItemDto itemDtoSaved = service.saveNewItem(userDtoSaved.getId(), itemDto);
        itemDto.setName("NewName");
        itemDto.setDescription("NewDescription");
        itemDto.setAvailable(false);

        try {
            service.updateItem(15, itemDtoSaved.getId(), itemDto);
        } catch (NotFoundException e) {
            assertThat(e.getMessage(), equalTo("This item not found"));
        }
    }


    @Test
    void getItemByIdForOwnerTest() {
        UserDto userDtoNotSaved = new UserDto();
        userDtoNotSaved.setName("Name");
        userDtoNotSaved.setEmail("e@mail.ru");
        UserDto userDtoSaved = userService.saveNewUser(userDtoNotSaved);
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Name");
        itemDto.setDescription("Description");
        itemDto.setAvailable(true);
        ItemDto itemDtoSaved = service.saveNewItem(userDtoSaved.getId(), itemDto);
        ItemDto itemFromService = service.getItemById(userDtoSaved.getId(), itemDtoSaved.getId());

        TypedQuery<Item> query = em.createQuery("SELECT i from Item i where i.id = :id",
                Item.class);
        Item item = query
                .setParameter("id", itemDtoSaved.getId())
                .getSingleResult();

        assertThat(item.getId(), notNullValue());
        assertThat(item.getName(), equalTo(itemFromService.getName()));
        assertThat(item.getDescription(), equalTo(itemFromService.getDescription()));
    }

    @Test
    void getItemByIdForTest() {
        UserDto userDtoNotSaved1 = new UserDto();
        userDtoNotSaved1.setName("Name1");
        userDtoNotSaved1.setEmail("e1@mail.ru");
        UserDto userDtoSaved1 = userService.saveNewUser(userDtoNotSaved1);
        UserDto userDtoNotSaved2 = new UserDto();
        userDtoNotSaved2.setName("Name2");
        userDtoNotSaved2.setEmail("e2@mail.ru");
        UserDto userDtoSaved2 = userService.saveNewUser(userDtoNotSaved2);
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Name");
        itemDto.setDescription("Description");
        itemDto.setAvailable(true);
        ItemDto itemDtoSaved = service.saveNewItem(userDtoSaved1.getId(), itemDto);
        ItemDto itemFromService = service.getItemById(userDtoSaved2.getId(), itemDtoSaved.getId());

        TypedQuery<Item> query = em.createQuery("SELECT i from Item i where i.id = :id",
                Item.class);
        Item item = query
                .setParameter("id", itemDtoSaved.getId())
                .getSingleResult();

        assertThat(item.getId(), notNullValue());
        assertThat(item.getName(), equalTo(itemFromService.getName()));
        assertThat(item.getDescription(), equalTo(itemFromService.getDescription()));
    }

    @Test
    void getItemByUserIdTest() {
        UserDto userDtoNotSaved = new UserDto();
        userDtoNotSaved.setName("Name");
        userDtoNotSaved.setEmail("e@mail.ru");
        UserDto userDtoSaved = userService.saveNewUser(userDtoNotSaved);
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Name");
        itemDto.setDescription("Description");
        itemDto.setAvailable(true);
        service.saveNewItem(userDtoSaved.getId(), itemDto);
        itemDto.setName("Name2");
        itemDto.setDescription("Description2");
        service.saveNewItem(userDtoSaved.getId(), itemDto);

        List<ItemDto> itemDtos = service.getItemByUserId(userDtoSaved.getId());

        assertThat(itemDtos.size(), equalTo(2));
    }

    @Test
    void getItemByWrongIdTest() {
        try {
            service.getItemById(1000);
        } catch (NotFoundException e) {
            assertThat(e.getMessage(), equalTo("Unknown item id"));
        }

        UserDto userDtoNotSaved = new UserDto();
        userDtoNotSaved.setName("Name");
        userDtoNotSaved.setEmail("e@mail.ru");
        UserDto userDtoSaved = userService.saveNewUser(userDtoNotSaved);

        try {
            service.getItemById(userDtoSaved.getId(), 1000);
        } catch (NotFoundException e) {
            assertThat(e.getMessage(), equalTo("Unknown item id"));
        }
    }

    @Test
    void getItemByIdWithWrongUserId() {
        UserDto userDtoNotSaved = new UserDto();
        userDtoNotSaved.setName("Name");
        userDtoNotSaved.setEmail("e@mail.ru");
        UserDto userDtoSaved = userService.saveNewUser(userDtoNotSaved);
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Name");
        itemDto.setDescription("Description");
        itemDto.setAvailable(true);
        service.saveNewItem(userDtoSaved.getId(), itemDto);
        itemDto.setName("Name2");
        itemDto.setDescription("Description2");
        ItemDto itemDtoFromService = service.saveNewItem(userDtoSaved.getId(), itemDto);

        try {
            service.getItemById(1000, itemDtoFromService.getId());
        } catch (NotFoundException e) {
            assertThat(e.getMessage(), equalTo("User not found"));
        }
    }

    @Test
    void getItemDtoByWrongIdTest() {
        try {
            service.getItemDtoById(1000);
        } catch (NotFoundException e) {
            assertThat(e.getMessage(), equalTo("Unknown item id"));
        }
    }

    @Test
    void getItemByUserIdWithPaginationTest() {
        UserDto userDtoNotSaved = new UserDto();
        userDtoNotSaved.setName("Name");
        userDtoNotSaved.setEmail("e@mail.ru");
        UserDto userDtoSaved = userService.saveNewUser(userDtoNotSaved);
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Name");
        itemDto.setDescription("Description");
        itemDto.setAvailable(true);
        service.saveNewItem(userDtoSaved.getId(), itemDto);
        itemDto.setName("Name2");
        itemDto.setDescription("Description2");
        service.saveNewItem(userDtoSaved.getId(), itemDto);

        List<ItemDto> itemDtos = service.getItemByUserId(userDtoSaved.getId(), 1, 1);

        assertThat(itemDtos.size(), equalTo(1));
    }

    @Test
    void getItemByUserIdWithWrongPaginationParametersTest() {
        UserDto userDtoNotSaved = new UserDto();
        userDtoNotSaved.setName("Name");
        userDtoNotSaved.setEmail("e@mail.ru");
        UserDto userDtoSaved = userService.saveNewUser(userDtoNotSaved);
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Name");
        itemDto.setDescription("Description");
        itemDto.setAvailable(true);
        service.saveNewItem(userDtoSaved.getId(), itemDto);
        itemDto.setName("Name2");
        itemDto.setDescription("Description2");
        service.saveNewItem(userDtoSaved.getId(), itemDto);

        try {
            service.getItemByUserId(userDtoSaved.getId(), -1, 1);
        } catch (ValidationException e) {
            assertThat(e.getMessage(), equalTo("The from parameter can't be negative number"));
        }

        try {
            service.getItemByUserId(userDtoSaved.getId(), 0, 0);
        } catch (ValidationException e) {
            assertThat(e.getMessage(), equalTo("The size parameter must be positive number"));
        }
    }

    @Test
    void searchItem() {
        UserDto userDtoNotSaved = new UserDto();
        userDtoNotSaved.setName("Name");
        userDtoNotSaved.setEmail("e@mail.ru");
        UserDto userDtoSaved = userService.saveNewUser(userDtoNotSaved);
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Hammer");
        itemDto.setDescription("Very good hammer");
        itemDto.setAvailable(true);
        service.saveNewItem(userDtoSaved.getId(), itemDto);
        itemDto.setName("Screwdriver");
        itemDto.setDescription("Very good screwdriver");
        service.saveNewItem(userDtoSaved.getId(), itemDto);

        List<ItemDto> itemDtos = service.search("screwdriver");

        assertThat(itemDtos.size(), equalTo(1));
        assertThat(itemDtos.get(0).getDescription(), equalTo(itemDto.getDescription()));
    }

    @Test
    void searchItemWithPagination() {
        UserDto userDtoNotSaved = new UserDto();
        userDtoNotSaved.setName("Name");
        userDtoNotSaved.setEmail("e@mail.ru");
        UserDto userDtoSaved = userService.saveNewUser(userDtoNotSaved);
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Hammer");
        itemDto.setDescription("Very good hammer");
        itemDto.setAvailable(true);
        service.saveNewItem(userDtoSaved.getId(), itemDto);
        itemDto.setName("Screwdriver");
        itemDto.setDescription("Very good screwdriver");
        service.saveNewItem(userDtoSaved.getId(), itemDto);
        service.saveNewItem(userDtoSaved.getId(), itemDto);

        List<ItemDto> itemDtos1 = service.search("screwdriver");

        assertThat(itemDtos1.size(), equalTo(2));

        List<ItemDto> itemDtos2 = service.search("screwdriver", 1, 1);
        assertThat(itemDtos2.size(), equalTo(1));
    }

    @Test
    void saveNewCommentTest() {
        UserDto userDtoNotSaved1 = new UserDto();
        userDtoNotSaved1.setName("Name");
        userDtoNotSaved1.setEmail("e@mail.ru");
        UserDto userDtoSaved1 = userService.saveNewUser(userDtoNotSaved1);

        UserDto userDtoNotSaved2 = new UserDto();
        userDtoNotSaved2.setName("Name2");
        userDtoNotSaved2.setEmail("e2@mail.ru");
        UserDto userDtoSaved2 = userService.saveNewUser(userDtoNotSaved2);

        ItemDto itemDto = new ItemDto();
        itemDto.setName("Name");
        itemDto.setDescription("Description");
        itemDto.setAvailable(true);
        ItemDto itemFromService = service.saveNewItem(userDtoSaved1.getId(), itemDto);

        Booking booking = new Booking();
        booking.setItem(ItemMapper.makeItem(itemFromService));
        booking.setBooker(UserMapper.makeUser(userDtoSaved2));
        booking.setStart(LocalDateTime.of(2022, 11, 20, 9, 0));
        booking.setEnd(LocalDateTime.of(2022, 11, 20, 10, 0));
        booking.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(booking);

        CommentsDto commentsDto = new CommentsDto();
        commentsDto.setText("good");
        service.saveNewComment(userDtoSaved2.getId(), itemFromService.getId(), commentsDto);

        TypedQuery<Comments> query = em.createQuery("SELECT c from Comments c where c.text = :text",
                Comments.class);
        Comments comments = query
                .setParameter("text", commentsDto.getText())
                .getSingleResult();

        assertThat(comments.getId(), notNullValue());
        assertThat(comments.getAuthorName(), equalTo(userDtoSaved2.getName()));
        assertThat(comments.getText(), equalTo(commentsDto.getText()));
        assertThat(comments.getItemId(), equalTo(itemFromService.getId()));
    }

    @Test
    void saveNewCommentWithoutTextTest() {
        UserDto userDtoNotSaved1 = new UserDto();
        userDtoNotSaved1.setName("Name");
        userDtoNotSaved1.setEmail("e@mail.ru");
        UserDto userDtoSaved1 = userService.saveNewUser(userDtoNotSaved1);

        UserDto userDtoNotSaved2 = new UserDto();
        userDtoNotSaved2.setName("Name2");
        userDtoNotSaved2.setEmail("e2@mail.ru");
        UserDto userDtoSaved2 = userService.saveNewUser(userDtoNotSaved2);

        ItemDto itemDto = new ItemDto();
        itemDto.setName("Name");
        itemDto.setDescription("Description");
        itemDto.setAvailable(true);
        ItemDto itemFromService = service.saveNewItem(userDtoSaved1.getId(), itemDto);

        Booking booking = new Booking();
        booking.setItem(ItemMapper.makeItem(itemFromService));
        booking.setBooker(UserMapper.makeUser(userDtoSaved2));
        booking.setStart(LocalDateTime.of(2022, 11, 20, 9, 0));
        booking.setEnd(LocalDateTime.of(2022, 11, 20, 10, 0));
        booking.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(booking);

        CommentsDto commentsDto = new CommentsDto();

        try {
            service.saveNewComment(userDtoSaved2.getId(), itemFromService.getId(), commentsDto);
        } catch (ValidationException e) {
            assertThat(e.getMessage(), equalTo("Comment text is empty"));
        }
    }

    @Test
    void saveNewCommentWithoutBookingTest() {
        UserDto userDtoNotSaved1 = new UserDto();
        userDtoNotSaved1.setName("Name");
        userDtoNotSaved1.setEmail("e@mail.ru");
        UserDto userDtoSaved1 = userService.saveNewUser(userDtoNotSaved1);

        UserDto userDtoNotSaved2 = new UserDto();
        userDtoNotSaved2.setName("Name2");
        userDtoNotSaved2.setEmail("e2@mail.ru");
        UserDto userDtoSaved2 = userService.saveNewUser(userDtoNotSaved2);

        ItemDto itemDto = new ItemDto();
        itemDto.setName("Name");
        itemDto.setDescription("Description");
        itemDto.setAvailable(true);
        ItemDto itemFromService = service.saveNewItem(userDtoSaved1.getId(), itemDto);

        CommentsDto commentsDto = new CommentsDto();
        commentsDto.setText("good");

        try {
            service.saveNewComment(userDtoSaved2.getId(), itemFromService.getId(), commentsDto);
        } catch (ValidationException e) {
            assertThat(e.getMessage(), equalTo("If you didn't rent this Item you can't leave the comment"));
        }
    }


    @Test
    void getItemsByRequestIdTest() {
        UserDto userDtoNotSaved = new UserDto();
        userDtoNotSaved.setName("Name");
        userDtoNotSaved.setEmail("e@mail.ru");
        UserDto userDtoSaved = userService.saveNewUser(userDtoNotSaved);

        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setDescription("description");

        ItemRequestDto itemRequestDtoFromService = itemRequestService.saveNewItemRequest(userDtoSaved.getId(),
                itemRequestDto);

        ItemDto itemDto = new ItemDto();
        itemDto.setName("Name");
        itemDto.setDescription("Description");
        itemDto.setAvailable(true);
        itemDto.setRequestId(itemRequestDtoFromService.getId());
        service.saveNewItem(userDtoSaved.getId(), itemDto);
        itemDto.setName("Name2");
        itemDto.setDescription("Description2");
        itemDto.setAvailable(true);
        itemDto.setRequestId(itemRequestDtoFromService.getId());
        service.saveNewItem(userDtoSaved.getId(), itemDto);

        List<ItemForRequestDto> itemDtos = service.getItemsByRequestId(itemRequestDtoFromService.getId());

        assertThat(itemDtos.size(), equalTo(2));
    }
}
