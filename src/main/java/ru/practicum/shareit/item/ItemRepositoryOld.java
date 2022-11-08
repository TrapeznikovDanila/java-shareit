package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepositoryOld {

    Item saveNewItem(long userId, Item item);

    Item updateItem(long userId, long itemId, Item item);

    Item getItemById(long itemId);

    List<Item> getItemByUserId(long userId);

    List<Item> search(String text);
}
