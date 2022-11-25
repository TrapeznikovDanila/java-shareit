package ru.practicum.shareit.booking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBookerId(long bookerId);

    Page<Booking> findByBookerId(long bookerId, Pageable pageable);

    @Query(value = "SELECT * from bookings " +
            "join items i on i.id = bookings.item_id " +
            "where user_id = ? " +
            "ORDER BY bookings.id DESC",
            nativeQuery = true)
    List<Booking> findBookingsByBookerIdJoinItem(long bookerId);

    List<Booking> findByItem_IdAndEndIsBeforeOrderByEndDesc(long itemId, LocalDateTime end);

    List<Booking> findByItem_IdAndStartIsAfterOrderByStartDesc(long itemId, LocalDateTime start);

    List<Booking> findBookingsByBookerId(long bookerId);
}
