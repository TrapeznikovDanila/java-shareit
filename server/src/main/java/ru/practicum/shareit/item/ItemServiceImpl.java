package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentsDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemForRequestDto;
import ru.practicum.shareit.item.model.Comments;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
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
        return ItemMapper.makeItemDto(itemRepository.save(item));
    }

    @Override
    public ItemDto updateItem(long userId, long itemId, ItemDto itemDto) {
        checkOwner(userId, itemId);
        ItemDto itemDtoFromDB = getItemDtoById(itemId);
        if (itemDto.getName() == null) {
            itemDto.setName(itemDtoFromDB.getName());
        }
        if (itemDto.getDescription() == null) {
            itemDto.setDescription(itemDtoFromDB.getDescription());
        }
        if (itemDto.getAvailable() == null) {
            itemDto.setAvailable(itemDtoFromDB.getAvailable());
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
    public List<ItemDto> getItemByUserId(long userId, Integer from, Integer size) {
        checkUserId(userId);
        Page<Item> items = itemRepository.findItemByUserIdOrderById(userId, PageRequest.of((from / size), size));
        List<Comments> allComments = commentRepository.findAll();
        List<ItemDto> itemsForOwnerDto = new ArrayList<>();
        List<Booking> bookings = bookingRepository.findAll().stream()
                .filter(b -> b.getStatus().equals(BookingStatus.APPROVED))
                .collect(Collectors.toList());
        for (Item item : items) {
            List<Booking> lastBookings = bookings.stream().filter(b -> b.getItem().getId() == item.getId())
                    .filter(b -> b.getEnd().isBefore(LocalDateTime.now())).sorted(Comparator.comparing(Booking::getEnd))
                    .collect(Collectors.toList());
            if (lastBookings.size() > 0) {
                item.setLastBooking(BookingMapper.makeBookingForItemDto(lastBookings.get(lastBookings.size() - 1)));
            }
            List<Booking> nextBookings = bookings.stream().filter(b -> b.getItem().getId() == item.getId())
                    .filter(b -> b.getStart().isAfter(LocalDateTime.now())).sorted(Comparator.comparing(Booking::getStart))
                    .collect(Collectors.toList());
            if (nextBookings.size() > 0) {
                item.setNextBooking(BookingMapper.makeBookingForItemDto(nextBookings.get(nextBookings.size() - 1)));
            }
            ItemDto itemDto = ItemMapper.makeItemDto(item);
            List<CommentsDto> comments = new ArrayList<>();
            for (Comments comment : allComments) {
                if (comment.getItemId() == item.getId()) {
                    comments.add(CommentsMapper.makeCommentDto(comment));
                }
            }
            itemDto.setComments(comments);
            itemsForOwnerDto.add(itemDto);
        }
        return itemsForOwnerDto;
    }

    @Override
    public List<ItemDto> search(String text, Integer from, Integer size) {
        if ((text == null) || text.isBlank()) {
            return Collections.emptyList();
        }
        Set<Item> items = Stream.concat(itemRepository
                                .findByNameContainingIgnoreCase(text)
                                .stream(),
                        itemRepository
                                .findByDescriptionContainingIgnoreCase(text)
                                .stream())
                .skip(from)
                .limit(size)
                .filter(item -> item.getAvailable())
                .collect(Collectors.toSet());

        return items.stream().map(ItemMapper::makeItemDto)
                .sorted((i1, i2) -> (int) (i1.getId() - i2.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public void checkOwner(long userId, long itemId) {
        Optional<Item> item = itemRepository.findById(itemId);
        if (item.isPresent()) {
            if (item.get().getUserId() != userId) {
                log.error(String.format("Item with id = %s not found", itemId));
                throw new NotFoundException(String.format("Item with id = %s not found", itemId));
            }
        } else {
            throw new NotFoundException("Unknown item id");
        }
    }

    @Override
    public void checkUserId(long userId) {
        if (!userRepository.existsById(userId)) {
            log.error(String.format("User with id = %s not found", userId));
            throw new NotFoundException(String.format("User with id = %s not found", userId));
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
        List<Booking> bookingsByUserId = bookingRepository.findBookingsByBookerId(userId)
                .stream()
                .filter(b -> b.getItem().getId() == itemId)
                .filter(b -> b.getStatus() == BookingStatus.APPROVED)
                .filter(b -> b.getEnd().isBefore(LocalDateTime.now()))
                .collect(Collectors.toList());
        if (bookingsByUserId.size() > 0) {
            commentDto.setItemId(itemId);
            commentDto.setCreated(LocalDateTime.now());
            commentDto.setAuthorName(userRepository.findById(userId).get().getName());
            Comments comment = CommentsMapper.makeComment(commentDto);
            comment.setUserId(userId);
            return CommentsMapper.makeCommentDto(commentRepository.save(comment));
        } else {
            throw new IllegalArgumentException("If you didn't rent this Item you can't leave the comment");
        }
    }

    @Override
    public List<ItemForRequestDto> getItemsByRequestId(long requestId) {
        return itemRepository.getItemByRequestId(requestId)
                .stream()
                .map(ItemMapper::makeItemForRequestDto)
                .collect(Collectors.toList());
    }

    private List<CommentsDto> getComments(long itemId) {
        return commentRepository.findCommentsByItemId(itemId)
                .stream()
                .map(CommentsMapper::makeCommentDto)
                .collect(Collectors.toList());
    }
}
