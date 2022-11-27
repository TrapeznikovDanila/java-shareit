package ru.practicum.shareit.request;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ItemRequestsRepository extends JpaRepository<ItemRequest, Long> {

    Page<ItemRequest> getItemRequestByUserIdOrderByCreated(long userId, Pageable pageable);

    @Query(value = "SELECT * from ITEM_REQUESTS where USER_ID != ? " +
            "ORDER BY CREATED Asc", nativeQuery = true)
    Page<ItemRequest> findAllNotForUserId(long bookerId, Pageable pageable);


}
