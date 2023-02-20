package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserDto;

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
public class UserServiceImplIntegrationTest {

    private final EntityManager em;

    private final UserServiceImpl service;

    @Test
    void saveNewUserTest() {
        UserDto userDto = new UserDto();
        userDto.setName("Name");
        userDto.setEmail("e@mail.ru");
        service.saveNewUser(userDto);

        TypedQuery<User> query = em.createQuery("SELECT u from User u where u.email = :email",
                User.class);
        User user = query
                .setParameter("email", userDto.getEmail())
                .getSingleResult();

        assertThat(user.getId(), notNullValue());
        assertThat(user.getName(), equalTo(userDto.getName()));
        assertThat(user.getEmail(), equalTo(userDto.getEmail()));
    }

    @Test
    void updateUserTest() {
        UserDto userDto = new UserDto();
        userDto.setName("Name");
        userDto.setEmail("e@mail.ru");
        UserDto userDtoSaved = service.saveNewUser(userDto);
        userDto.setName("NewName");
        UserDto userDtoFromService = service.updateUser(userDtoSaved.getId(), userDto);

        TypedQuery<User> query = em.createQuery("SELECT u from User u where u.id = :id",
                User.class);
        User user = query
                .setParameter("id", userDtoFromService.getId())
                .getSingleResult();

        assertThat(user.getId(), notNullValue());
        assertThat(user.getName(), equalTo(userDto.getName()));
        assertThat(user.getEmail(), equalTo(userDto.getEmail()));
    }

    @Test
    void getUserByIdTest() {
        UserDto userDto = new UserDto();
        userDto.setName("Name");
        userDto.setEmail("e@mail.ru");
        UserDto userDtoSaved = service.saveNewUser(userDto);
        UserDto userDtoFromService = service.getUserById(userDtoSaved.getId());

        TypedQuery<User> query = em.createQuery("SELECT u from User u where u.id = :id",
                User.class);
        User user = query
                .setParameter("id", userDtoFromService.getId())
                .getSingleResult();

        assertThat(user.getId(), notNullValue());
        assertThat(user.getName(), equalTo(userDto.getName()));
        assertThat(user.getEmail(), equalTo(userDto.getEmail()));
    }

    @Test
    void deleteUserTest() {
        UserDto userDto = new UserDto();
        userDto.setName("Name");
        userDto.setEmail("e@mail.ru");
        UserDto userDtoSaved = service.saveNewUser(userDto);
        service.deleteUser(userDtoSaved.getId());

        List<UserDto> userDtos = service.getAllUsers();

        assertThat(userDtos.size(), equalTo(0));
    }

    @Test
    void getAllUsersTest() {
        UserDto userDto1 = new UserDto();
        userDto1.setName("Name1");
        userDto1.setEmail("e1@mail.ru");
        service.saveNewUser(userDto1);

        UserDto userDto2 = new UserDto();
        userDto2.setName("Name2");
        userDto2.setEmail("e2@mail.ru");
        service.saveNewUser(userDto2);

        List<UserDto> userDtos = service.getAllUsers();

        assertThat(userDtos.size(), equalTo(2));
    }
}
