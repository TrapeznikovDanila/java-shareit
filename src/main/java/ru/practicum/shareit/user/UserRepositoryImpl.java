package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.ValidationException;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class UserRepositoryImpl implements UserRepository {

    private HashMap<Long, User> users = new HashMap<>();
    private Long id = Long.valueOf(0);

    @Override
    public List<User> getAllUsers() {
        return users.values().stream().collect(Collectors.toList());
    }

    @Override
    public User getUserById(Long userId) {
        return users.get(userId);
    }

    @Override
    public User saveNewUser(User user) {
        validation(user);
        user.setId(++id);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        checkId(user.getId());
        checkEmail(user.getEmail());
        User updatedUser = users.get(user.getId());
        if (user.getEmail() != null) {
            updatedUser.setEmail(user.getEmail());
        }
        if (user.getName() != null) {
            updatedUser.setName(user.getName());
        }
        users.put(user.getId(), updatedUser);
        return updatedUser;
    }

    @Override
    public void deleteUser(Long userId) {
        users.remove(userId);
    }

    @Override
    public HashMap<Long, User> getUsers() {
        return users;
    }

    private void validation(User user) {
        if (user.getEmail() == null
                || user.getEmail().isBlank()
                || !user.getEmail().contains("@")) {
            log.error("Email error");
            throw new ValidationException("Email error");
        } else {
            checkEmail(user.getEmail());
        }
    }

    private void checkEmail(String email) {
        List<String> emails = users.values().stream().map(User::getEmail).collect(Collectors.toList());
        for (String s : emails) {
            if (s.equals(email)) {
                log.error("This email is already being used by another user");
                throw new ConflictException("This email is already being used by another user");
            }
        }
    }

    private void checkId(long userId) {
        if (!users.containsKey(userId)) {
            log.error("Unknown user id");
            throw new ValidationException("Unknown user id");
        }
    }
}
