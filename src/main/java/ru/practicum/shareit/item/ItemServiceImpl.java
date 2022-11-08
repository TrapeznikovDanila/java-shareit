package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentsDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comments;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    private final CommentsRepository commentRepository;

    @Override
    public ItemDto saveNewItem(long userId, ItemDto itemDto) {
        checkUserId(userId);
        Item item = ItemMapper.makeItem(itemDto);
        item.setUserId(userId);
        validation(item);
        return ItemMapper.makeItemDto(itemRepository.save(item));
    }

    @Override
    public ItemDto updateItem(long userId, long itemId, ItemDto itemDto) {
        checkOwner(userId, itemId);
        ItemDto itemDto1 = getItemDtoById(itemId);
        if (itemDto.getName() == null) {
            itemDto.setName(itemDto1.getName());
        }
        if (itemDto.getDescription() == null) {
            itemDto.setDescription(itemDto1.getDescription());
        }
        if (itemDto.getAvailable() == null) {
            itemDto.setAvailable(itemDto1.getAvailable());
        }
        itemDto.setId(itemId);
        Item item = ItemMapper.makeItem(itemDto);
        item.setUserId(userId);
        return ItemMapper.makeItemDto(itemRepository.save(item));
    }

    @Override
    public ItemDto getItemDtoById(long itemId) {
        Optional<Item> item = itemRepository.findById(itemId);
        if (item.isPresent()) {
            return ItemMapper.makeItemDto(item.get());
        } else {
            throw new NotFoundException("Unknown item id");
        }
    }

    @Override
    public Item getItemById(long itemId) {
        Optional<Item> item = itemRepository.findById(itemId);
        if (item.isPresent()) {
            return item.get();
        } else {
            throw new NotFoundException("Unknown item id");
        }
    }

    @Override
    public ItemDto getItemById(long userId, long itemId) {
        Optional<Item> itemOptional = itemRepository.findById(itemId);
        if (itemOptional.isPresent()) {
            Item item = itemOptional.get();
            if (item.getUserId() == userId) {
                ItemDto itemForOwnerDto = setLastAndNextBooking(ItemMapper.makeItemDto(item), item.getId());
                itemForOwnerDto.setComments(getComments(itemId));
                return itemForOwnerDto;
            } else {
                ItemDto itemForUserDto = ItemMapper.makeItemDto(item);
                itemForUserDto.setComments(getComments(itemId));
                return itemForUserDto;
            }
        } else {
            throw new NotFoundException("Unknown item id");
        }
    }


    @Override
    public List<ItemDto> getItemByUserId(long userId) {
        List<Item> items = itemRepository.findItemByUserId(userId);
        List<ItemDto> itemsForOwnerDto = new ArrayList<>();
        for (Item item : items) {
            ItemDto itemDto = setLastAndNextBooking(ItemMapper.makeItemDto(item), item.getId());
            itemDto.setComments(commentRepository.findCommentsByItemId(item.getId())
                    .stream()
                    .map(CommentsMapper::makeCommentDto)
                    .collect(Collectors.toList()));
            itemsForOwnerDto.add(itemDto);
        }
        return itemsForOwnerDto;
    }

    @Override
    public List<ItemDto> search(String text) {
        Set<Item> items = Stream.concat(itemRepository
                                .findByNameContainingIgnoreCase(text)
                                .stream(),
                        itemRepository
                                .findByDescriptionContainingIgnoreCase(text)
                                .stream())
                .filter(item -> item.getAvailable() == true)
                .collect(Collectors.toSet());

        return items.stream().map(ItemMapper::makeItemDto)
                .sorted((i1, i2) -> (int) (i1.getId() - i2.getId()))
                .collect(Collectors.toList());
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

    @Override
    public void checkOwner(long userId, long itemId) {
        Optional<Item> item = itemRepository.findById(itemId);
        if (item.isPresent()) {
            if (item.get().getUserId() != userId) {
                log.error("This item not found");
                throw new NotFoundException("This item not found");
            }
        } else {
            throw new NotFoundException("Unknown item id");
        }
    }

    @Override
    public void checkUserId(long userId) {
        if (!userRepository.existsById(userId)) {
            log.error("User not found");
            throw new NotFoundException("User not found");
        }
    }

    private ItemDto setLastAndNextBooking(ItemDto itemDto, long itemId) {
        List<Booking> lastBookings = bookingRepository
                .findByItem_IdAndEndIsBeforeOrderByEndDesc(itemId, LocalDateTime.now());
        if (lastBookings.size() > 0) {
            itemDto.setLastBooking(BookingMapper
                    .makeBookingForItemDto(lastBookings.get(lastBookings.size() - 1)));
        }
        List<Booking> nextBookings = bookingRepository
                .findByItem_IdAndStartIsAfterOrderByStartDesc(itemId, LocalDateTime.now());
        if (nextBookings.size() > 0) {
            itemDto.setNextBooking(BookingMapper
                    .makeBookingForItemDto(nextBookings.get(nextBookings.size() - 1)));
        }
        return itemDto;
    }

    @Override
    public CommentsDto saveNewComment(long userId, long itemId, CommentsDto commentDto) {
        checkUserId(userId);
        commentsValidation(commentDto);
        List<Booking> bookings1 = bookingRepository.findBookingsByBookerId(userId)
                .stream()
                .filter(b -> b.getItem().getId() == itemId)
                .filter(b -> b.getStatus() == BookingStatus.APPROVED)
                .filter(b -> b.getEnd().isBefore(LocalDateTime.now()))
                .collect(Collectors.toList());
        if (bookings1.size() > 0) {
            commentDto.setItemId(itemId);
            commentDto.setCreated(LocalDateTime.now());
            commentDto.setAuthorName(userRepository.findById(userId).get().getName());
            Comments comment = CommentsMapper.makeComment(commentDto);
            comment.setUserId(userId);
            return CommentsMapper.makeCommentDto(commentRepository.save(comment));
        } else {
            throw new ValidationException("If you didn't rent this Item you can't leave the comment");
        }
    }

    private void commentsValidation(CommentsDto commentDto) {
        if ((commentDto.getText() == null) || commentDto.getText().isBlank()) {
            throw new ValidationException("Comment text is empty");
        }
    }

    private List<CommentsDto> getComments(long itemId) {
        return commentRepository.findCommentsByItemId(itemId)
                .stream()
                .map(CommentsMapper::makeCommentDto)
                .collect(Collectors.toList());
    }
}
