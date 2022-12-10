package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentsDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemForRequestDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    ItemDto saveNewItem(long userId, ItemDto itemDto);

    ItemDto updateItem(long userId, long itemId, ItemDto itemDto);

    ItemDto getItemById(long userId, long itemId);

    ItemDto getItemDtoById(long itemId);

    Item getItemById(long itemId);

    List<ItemDto> getItemByUserId(long userId, Integer from, Integer size);

    List<ItemDto> search(String text, Integer from, Integer size);

    void checkOwner(long userId, long itemId);

    void checkUserId(long userId);

    CommentsDto saveNewComment(long userId, long itemId, CommentsDto commentDto);

    List<ItemForRequestDto> getItemsByRequestId(long requestId);
}
