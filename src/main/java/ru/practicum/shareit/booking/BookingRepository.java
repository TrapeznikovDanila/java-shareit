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

    @Query(value = "SELECT * from bookings where BOOKER_ID = ?1 " +
            "and start_date < ?2 and END_DATE > ?2 ORDER BY START_DATE DESC", nativeQuery = true)
    Page<Booking> findCurrentBookingsByBookerId(long bookerId, LocalDateTime now, Pageable pageable);

    @Query(value = "SELECT * from bookings where BOOKER_ID = ? " +
            "and start_date > ? ORDER BY START_DATE DESC", nativeQuery = true)
    Page<Booking> findFutureBookingsByBookerId(long bookerId, LocalDateTime now, Pageable pageable);

    @Query(value = "SELECT * from bookings where BOOKER_ID = ? " +
            "and end_date < ? ORDER BY START_DATE DESC", nativeQuery = true)
    Page<Booking> findPastBookingsByBookerId(long bookerId, LocalDateTime now, Pageable pageable);

    Page<Booking> findBookingByBooker_IdAndStatusIs(long bookerId, BookingStatus status, Pageable pageable);

    Page<Booking> findByItem_userIdAndEndAfterAndStartBeforeOrderByStartDesc(long userId, LocalDateTime now1,
                                                                             LocalDateTime now2, Pageable pageable);

    Page<Booking> findByItem_userIdAndStartAfterOrderByStartDesc(long userId, LocalDateTime now, Pageable pageable);

    Page<Booking> findByItem_userIdAndEndBeforeOrderByStartDesc(long userId, LocalDateTime now, Pageable pageable);

    Page<Booking> findByItem_UserIdAndStatusOrderByStartDesc(long userId, BookingStatus status, Pageable pageable);

    Page<Booking> findByItem_UserIdOrderByStartDesc(long userId, Pageable pageable);

    List<Booking> findByItem_IdAndEndIsBeforeOrderByEndDesc(long itemId, LocalDateTime end);

    List<Booking> findByItem_IdAndStartIsAfterOrderByStartDesc(long itemId, LocalDateTime start);

    List<Booking> findBookingsByBookerId(long bookerId);
}
