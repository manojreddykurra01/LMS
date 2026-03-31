package com.library.service;

import com.library.exception.BadRequestException;
import com.library.exception.ResourceNotFoundException;
import com.library.model.Book;
import com.library.model.Reservation;
import com.library.model.ReservationStatus;
import com.library.model.User;
import com.library.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final BookService bookService;
    private final UserService userService;

    @Transactional
    public Reservation reserveBook(Long userId, Long bookId) {
        User user = userService.getUserById(userId);
        Book book = bookService.getBookById(bookId);

        if (book.getAvailableCopies() > 0) {
            throw new BadRequestException("Book is available, you can borrow it directly without reservation");
        }

        Reservation reservation = Reservation.builder()
                .user(user)
                .book(book)
                .reservationDate(LocalDate.now())
                .status(ReservationStatus.PENDING)
                .build();

        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + reservationId));

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new BadRequestException("Only pending reservations can be cancelled");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        return reservationRepository.save(reservation);
    }

    public List<Reservation> getUserReservations(Long userId) {
        User user = userService.getUserById(userId);
        return reservationRepository.findByUserOrderByReservationDateDesc(user);
    }
    
    public List<Reservation> getPendingReservationsForBook(Long bookId) {
        Book book = bookService.getBookById(bookId);
        return reservationRepository.findByBookAndStatusOrderByReservationDateAsc(book, ReservationStatus.PENDING);
    }
}
