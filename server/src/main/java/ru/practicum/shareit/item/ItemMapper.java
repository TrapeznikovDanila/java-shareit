package ru.practicum.shareit.item;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemForRequestDto;
import ru.practicum.shareit.item.model.Item;

@Component
public class ItemMapper {
    public static Item makeItem(ItemDto itemDto) {
        Item item = new Item();
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable());
        item.setId(itemDto.getId());
        item.setRequestId(itemDto.getRequestId());
        item.setLastBooking(itemDto.getLastBooking());
        item.setNextBooking(itemDto.getNextBooking());
        return item;
    }

    public static ItemDto makeItemDto(Item item) {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(item.getId());
        itemDto.setName(item.getName());
        itemDto.setDescription(item.getDescription());
        itemDto.setAvailable(item.getAvailable());
        itemDto.setRequestId(item.getRequestId());
        itemDto.setLastBooking(item.getLastBooking());
        itemDto.setNextBooking(item.getNextBooking());
        return itemDto;
    }

    public static ItemForRequestDto makeItemForRequestDto(Item item) {
        ItemForRequestDto itemDto = new ItemForRequestDto();
        itemDto.setId(item.getId());
        itemDto.setName(item.getName());
        itemDto.setDescription(item.getDescription());
        itemDto.setAvailable(item.getAvailable());
        itemDto.setRequestId(item.getRequestId());
        return itemDto;
    }
}
