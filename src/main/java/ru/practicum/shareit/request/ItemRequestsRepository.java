package ru.practicum.shareit.request;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRequestsRepository extends JpaRepository<ItemRequest, Long> {

    List<ItemRequest> getItemRequestByUserId(long userId);
}