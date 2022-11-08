package ru.practicum.shareit.user;

import java.util.HashMap;
import java.util.List;

public interface UserRepositoryOld {
    List<User> getAllUsers();

    User getUserById(Long userId);

    User saveNewUser(User user);

    User updateUser(User user);

    void deleteUser(Long userId);

    HashMap<Long, User> getUsers();
}
