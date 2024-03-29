package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestServiceImplIntegrationTest {

    private final UserRepository userRepository;

    private final ItemRequestService service;

    private final EntityManager em;

    @Test
    void saveNewItemRequestTest() {
        User user = new User();
        user.setName("Name");
        user.setEmail("e@mail.ru");
        User savedUser = userRepository.save(user);

        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setDescription("description");

        ItemRequestDto itemRequestDtoFromService = service.saveNewItemRequest(savedUser.getId(), itemRequestDto);

        TypedQuery<ItemRequest> query = em.createQuery("select i from ItemRequest i where i.id = :id",
                ItemRequest.class);
        ItemRequest itemRequest = query.setParameter("id",
                itemRequestDtoFromService.getId()).getSingleResult();

        assertThat(itemRequest.getId(), notNullValue());
        assertThat(itemRequest.getCreated(), notNullValue());
        assertThat(itemRequest.getDescription(), equalTo(itemRequestDto.getDescription()));
    }

    @Test
    void saveNewItemRequestWithWrongUserIdGetValidationExceptionTest() {
        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setDescription("description");

        try {
            service.saveNewItemRequest(100, itemRequestDto);
        } catch (NotFoundException e) {
            assertThat(e.getMessage(), equalTo(String.format("User with id = %s not found", 100)));
        }
    }

    @Test
    void getItemRequestByAuthorTest() {
        User user = new User();
        user.setName("Name");
        user.setEmail("e@mail.ru");
        User savedUser = userRepository.save(user);

        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setDescription("description");
        ItemRequestDto itemRequestDtoFromService = service.saveNewItemRequest(savedUser.getId(), itemRequestDto);

        Item item = new Item();
        item.setName("name");
        item.setDescription("description");
        item.setUserId(savedUser.getId());
        item.setRequestId(itemRequestDtoFromService.getId());

        List<ItemRequestDto> itemRequestDtoFromServiceList = service.getItemRequestByAuthor(savedUser.getId(),
                0, 1);

        assertThat(itemRequestDtoFromServiceList.size(), equalTo(1));
        assertThat(itemRequestDtoFromServiceList.get(0).getDescription(), equalTo(itemRequestDto.getDescription()));
    }

    @Test
    void getItemRequestsTest() {
        User user1 = new User();
        user1.setName("Name1");
        user1.setEmail("e1@mail.ru");
        User savedUser1 = userRepository.save(user1);

        User user2 = new User();
        user2.setName("Name2");
        user2.setEmail("e2@mail.ru");
        User savedUser2 = userRepository.save(user2);

        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setDescription("description");
        service.saveNewItemRequest(savedUser1.getId(), itemRequestDto);

        List<ItemRequestDto> itemRequestDtoFromServiceListForUser1 = service.getItemRequests(savedUser1.getId(),
                0, 5);

        List<ItemRequestDto> itemRequestDtoFromServiceListForUser2 = service.getItemRequests(savedUser2.getId(),
                0, 5);

        assertThat(itemRequestDtoFromServiceListForUser1.size(), equalTo(0));
        assertThat(itemRequestDtoFromServiceListForUser2.size(), equalTo(1));
    }

    @Test
    void findAllTest() {
        User user = new User();
        user.setName("Name");
        user.setEmail("e@mail.ru");
        User savedUser = userRepository.save(user);

        ItemRequestDto itemRequestDto1 = new ItemRequestDto();
        itemRequestDto1.setDescription("description1");
        service.saveNewItemRequest(savedUser.getId(), itemRequestDto1);

        ItemRequestDto itemRequestDto2 = new ItemRequestDto();
        itemRequestDto2.setDescription("description1");
        service.saveNewItemRequest(savedUser.getId(), itemRequestDto2);

        List<ItemRequestDto> itemRequestDtoFromServiceList = service.findAll(savedUser.getId());

        assertThat(itemRequestDtoFromServiceList.size(), equalTo(2));
    }

    @Test
    void getItemRequestByIdTest() {
        User user = new User();
        user.setName("Name");
        user.setEmail("e@mail.ru");
        User savedUser = userRepository.save(user);

        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setDescription("description1");
        ItemRequestDto itemRequestDtoToService = service.saveNewItemRequest(savedUser.getId(), itemRequestDto);

        ItemRequestDto itemRequestDtoFromService = service.getItemRequestById(savedUser.getId(),
                itemRequestDtoToService.getId());

        assertThat(itemRequestDtoToService.getDescription(), equalTo(itemRequestDtoFromService.getDescription()));
    }
}
