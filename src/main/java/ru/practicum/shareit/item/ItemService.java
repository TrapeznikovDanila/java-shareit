package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {

    ItemDto saveNewItem(long userId, ItemDto itemDto);

    ItemDto updateItem(long userId, long itemId, ItemDto itemDto);

    ItemDto getItemById(long itemId);

    List<ItemDto> getItemByUserId(long userId);

    List<ItemDto> search(String text);
}
