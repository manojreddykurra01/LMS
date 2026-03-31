package com.library.repository;

import com.library.model.Reservation;
import com.library.model.ReservationStatus;
import com.library.model.User;
import com.library.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUserOrderByReservationDateDesc(User user);
    List<Reservation> findByBookAndStatusOrderByReservationDateAsc(Book book, ReservationStatus status);
}
