package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepositoryOld;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Repository
public class ItemRepositoryImpl implements ItemRepositoryOld {

    private final UserRepositoryOld userRepository;

    private Long id = 0L;

    @Override
    public Item saveNewItem(long userId, Item item) {
        checkUserId(userId);
        validation(item);
        item.setId(++id);
        item.setUserId(userId);
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item updateItem(long userId, long itemId, Item item) {
        checkOwner(userId, itemId);
        Item updatedItem = items.get(itemId);
        if (item.getName() != null) {
            updatedItem.setName(item.getName());
        }
        if (item.getDescription() != null) {
            updatedItem.setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            updatedItem.setAvailable(item.getAvailable());
        }
        items.put(itemId, updatedItem);
        return updatedItem;
    }

    @Override
    public Item getItemById(long itemId) {
        return items.get(itemId);
    }

    @Override
    public List<Item> getItemByUserId(long userId) {
        return items.values().stream().filter(i -> i.getUserId() == userId).collect(Collectors.toList());
    }

    @Override
    public List<Item> search(String text) {
        List<Item> searchResult = new ArrayList<>();
        for (Item item : items.values()) {
            if ((item.getName().toLowerCase().indexOf(text.toLowerCase()) != -1)
                    || (item.getDescription().toLowerCase().indexOf(text.toLowerCase()) != -1)) {
                if (item.getAvailable() == true) {
                    searchResult.add(item);
                }
            }
        }
        return searchResult;
    }

    private void validation(Item item) {
        if (item.getAvailable() == null) {
            log.error("The availability field cannot be empty");
            throw new ValidationException("The availability field cannot be empty");
        }
        if (item.getName() == null || item.getName().isBlank()) {
            log.error("The name field cannot be empty");
            throw new ValidationException("The name field cannot be empty");
        }
        if (item.getDescription() == null || item.getDescription().isBlank()) {
            log.error("The description field cannot be empty");
            throw new ValidationException("The description field cannot be empty");
        }
    }

    private void checkOwner(long userId, long itemId) {
        if (items.get(itemId).getUserId() != userId) {
            log.error("This item not found");
            throw new NotFoundException("This item not found");
        }
    }

    private void checkUserId(long userId) {
        if (!userRepository.getUsers().keySet().contains(userId)) {
            log.error("User not found");
            throw new NotFoundException("User not found");
        }
    }
}
