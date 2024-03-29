package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemForRequestDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemRequestsRepository itemRequestsRepository;

    @Override
    public ItemRequestDto saveNewItemRequest(long userId, ItemRequestDto itemRequestDto) {
        checkUserId(userId);
        ItemRequest itemRequest = ItemRequestMapper.makeItemRequest(itemRequestDto);
        itemRequest.setCreated(LocalDateTime.now());
        itemRequest.setUserId(userId);
        return ItemRequestMapper.makeItemRequestDto(itemRequestsRepository.save(itemRequest));
    }

    @Override
    public List<ItemRequestDto> getItemRequestByAuthor(long userId, Integer from, Integer size) {
        checkUserId(userId);
        List<ItemRequestDto> itemsRequests = itemRequestsRepository.getItemRequestByUserIdOrderByCreated(userId,
                        PageRequest.of(from / size, size))
                .stream()
                .map(ItemRequestMapper::makeItemRequestDto)
                .collect(Collectors.toList());
        List<Item> items = itemRepository.findAll();
        for (ItemRequestDto itemRequest : itemsRequests) {
            List<ItemForRequestDto> itemForRequestDtos = new ArrayList<>();
            for (Item item : items) {
                if (item.getRequestId() == itemRequest.getId()) {
                    itemForRequestDtos.add(ItemMapper.makeItemForRequestDto(item));
                }
            }
            itemRequest.setItems(itemForRequestDtos);
        }
        return itemsRequests;
    }

    @Override
    public List<ItemRequestDto> getItemRequests(long userId, int from, int size) {
        checkUserId(userId);
        List<ItemRequestDto> itemsRequests = itemRequestsRepository.findAllNotForUserId(userId,
                        PageRequest.of(from / size, size))
                .stream()
                .map(ItemRequestMapper::makeItemRequestDto)
                .collect(Collectors.toList());
        List<Item> items = itemRepository.findAll();
        for (ItemRequestDto itemRequest : itemsRequests) {
            List<ItemForRequestDto> itemForRequestDtos = new ArrayList<>();
            for (Item item : items) {
                if (item.getRequestId() == itemRequest.getId()) {
                    itemForRequestDtos.add(ItemMapper.makeItemForRequestDto(item));
                }
            }
            itemRequest.setItems(itemForRequestDtos);
        }
        return itemsRequests;
    }

    @Override
    public List<ItemRequestDto> findAll(long userId) {
        checkUserId(userId);
        return itemRequestsRepository.findAll()
                .stream()
                .map(ItemRequestMapper::makeItemRequestDto)
                .sorted(Comparator.comparing(ItemRequestDto::getCreated))
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestDto getItemRequestById(long userId, long itemRequestId) {
        checkUserId(userId);
        Optional<ItemRequest> itemRequestOptional = itemRequestsRepository.findById(itemRequestId);
        if (itemRequestOptional.isPresent()) {
            ItemRequestDto itemRequestDto = ItemRequestMapper.makeItemRequestDto(itemRequestOptional.get());
            itemRequestDto.setItems(itemRepository.getItemByRequestId(itemRequestDto.getId())
                    .stream()
                    .map(ItemMapper::makeItemForRequestDto)
                    .collect(Collectors.toList()));
            return itemRequestDto;
        } else {
            throw new NotFoundException("Unknown item request id");
        }
    }

    private void checkUserId(long userId) {
        if (!userRepository.existsById(userId)) {
            log.error(String.format("User with id = %s not found", userId));
            throw new NotFoundException(String.format("User with id = %s not found", userId));
        }
    }
}
