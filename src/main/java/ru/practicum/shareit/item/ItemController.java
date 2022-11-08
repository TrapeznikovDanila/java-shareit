package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentsDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Collections;
import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;


    @PostMapping
    public ItemDto saveNewItem(@RequestHeader("X-Sharer-User-Id") long userId, @RequestBody ItemDto itemDto) {
        return itemService.saveNewItem(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestHeader("X-Sharer-User-Id") long userId,
                              @PathVariable long itemId,
                              @RequestBody ItemDto itemDto) {
        return itemService.updateItem(userId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemById(@RequestHeader("X-Sharer-User-Id") long userId,
                               @PathVariable long itemId) {
        return itemService.getItemById(userId, itemId);
    }

    @GetMapping
    public List<ItemDto> getItemsByUserId(@RequestHeader("X-Sharer-User-Id") long userId) {
        return itemService.getItemByUserId(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam(required = false) String text) {
        if ((text == null) || text.isBlank()) {
            return Collections.emptyList();
        }
        return itemService.search(text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentsDto saveNewComment(@RequestHeader("X-Sharer-User-Id") long userId,
                                      @PathVariable long itemId,
                                      @RequestBody CommentsDto commentDto) {
        return itemService.saveNewComment(userId, itemId, commentDto);
    }
}
