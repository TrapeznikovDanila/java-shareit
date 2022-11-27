package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

/**
 * TODO Sprint add-item-requests.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {

    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDto saveNewItemRequest(@RequestHeader("X-Sharer-User-Id") long userId,
                                             @RequestBody ItemRequestDto itemRequestDto) {
        return itemRequestService.saveNewItemRequest(userId, itemRequestDto);
    }

    @GetMapping
    public List<ItemRequestDto> getItemRequestByAuthor(@RequestHeader("X-Sharer-User-Id") long userId,
                                                       @RequestParam(required = false) Integer from,
                                                       @RequestParam(required = false) Integer size) {
        return itemRequestService.getItemRequestByAuthor(userId, from, size);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getItemRequests(@RequestHeader("X-Sharer-User-Id") long userId,
                                                @RequestParam(required = false) Integer from,
                                                @RequestParam(required = false) Integer size) {
        if ((from == null) || (size == null)) {
            return itemRequestService.findAll(userId);
        }
        return itemRequestService.getItemRequests(userId, from, size);
    }

    @GetMapping("/{itemRequestId}")
    public ItemRequestDto getItemRequestById(@RequestHeader("X-Sharer-User-Id") long userId,
                                             @PathVariable long itemRequestId) {
        return itemRequestService.getItemRequestById(userId, itemRequestId);
    }


}
