package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::makeUserDto).collect(Collectors.toList());
    }

    @Override
    public UserDto getUserById(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            return UserMapper.makeUserDto(user.get());
        } else {
            throw new NotFoundException("Unknown user id");
        }
    }

    @Override
    public UserDto saveNewUser(UserDto userDto) {
        User user = UserMapper.makeUser(userDto);
        validation(user);
        return UserMapper
                .makeUserDto(userRepository.save(user));
    }

    @Override
    public UserDto updateUser(Long userId, UserDto userDto) {
        UserDto userDto1 = getUserById(userId);
        if (userDto.getName() == null) {
            userDto.setName(userDto1.getName());
        }
        if (userDto.getEmail() == null) {
            userDto.setEmail(userDto1.getEmail());
        }
        userDto.setId(userId);
        return UserMapper
                .makeUserDto(userRepository.save(UserMapper.makeUser(userDto)));

    }

    @Override
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    private void validation(User user) {
        if (user.getEmail() == null
                || user.getEmail().isBlank()
                || !user.getEmail().contains("@")) {
            log.error("Email error");
            throw new ValidationException("Email error");
        }
    }
}
