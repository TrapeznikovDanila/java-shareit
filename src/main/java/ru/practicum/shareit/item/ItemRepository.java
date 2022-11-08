package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByNameContainingIgnoreCase(String text);

    List<Item> findByDescriptionContainingIgnoreCase(String text);

    List<Item> findItemByUserId(long userId);
}
