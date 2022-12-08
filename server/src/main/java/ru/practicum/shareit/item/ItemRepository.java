package ru.practicum.shareit.item;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByNameContainingIgnoreCase(String text);

    List<Item> findByDescriptionContainingIgnoreCase(String text);

//    List<Item> findItemByUserId(long userId);

    Page<Item> findItemByUserIdOrderById(long userId, Pageable pageable);

    List<Item> getItemByRequestId(long requestId);
}
