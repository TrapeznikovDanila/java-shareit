package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {

    ItemRequestDto saveNewItemRequest(long userId, ItemRequestDto itemRequestDto);

    List<ItemRequestDto> getItemRequestByAuthor(long userId, Integer from, Integer size);

    List<ItemRequestDto> getItemRequests(long userId, int from, int size);

    List<ItemRequestDto> findAll(long userId);

    ItemRequestDto getItemRequestById(long userId, long itemRequestId);

}
