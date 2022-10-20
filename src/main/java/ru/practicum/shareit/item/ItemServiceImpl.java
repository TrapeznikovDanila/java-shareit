package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;

    @Override
    public ItemDto saveNewItem(long userId, ItemDto itemDto) {
        return ItemMapper.makeItemDto(itemRepository
                .saveNewItem(userId, ItemMapper.makeItem(itemDto)));
    }

    @Override
    public ItemDto updateItem(long userId, long itemId, ItemDto itemDto) {
        return ItemMapper.makeItemDto(itemRepository
                .updateItem(userId, itemId, ItemMapper.makeItem(itemDto)));
    }

    @Override
    public ItemDto getItemById(long itemId) {
        return ItemMapper.makeItemDto(itemRepository.getItemById(itemId));
    }

    @Override
    public List<ItemDto> getItemByUserId(long userId) {
        return itemRepository.getItemByUserId(userId)
                .stream()
                .map(ItemMapper::makeItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text) {
        return itemRepository.search(text)
                .stream()
                .map(ItemMapper::makeItemDto)
                .collect(Collectors.toList());
    }
}
