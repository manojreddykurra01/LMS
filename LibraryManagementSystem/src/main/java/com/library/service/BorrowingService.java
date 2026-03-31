package com.library.service;

import com.library.exception.BadRequestException;
import com.library.exception.ResourceNotFoundException;
import com.library.model.*;
import com.library.repository.BorrowRecordRepository;
import com.library.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BorrowingService {

    private final BorrowRecordRepository borrowRecordRepository;
    private final ReservationRepository reservationRepository;
    private final BookService bookService;
    private final UserService userService;
    private final NotificationService notificationService;

    @Transactional
    public BorrowRecord borrowBook(Long userId, Long bookId) {
        User user = userService.getUserById(userId);
        Book book = bookService.getBookById(bookId);

        if (book.getAvailableCopies() <= 0) {
            throw new BadRequestException("Book is currently unavailable for borrowing");
        }

        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookService.updateBook(book.getId(), book);

        BorrowRecord record = BorrowRecord.builder()
                .user(user)
                .book(book)
                .borrowDate(LocalDate.now())
                .expectedReturnDate(LocalDate.now().plusDays(14)) // 14 days borrowing period
                .status(BorrowStatus.BORROWED)
                .build();

        BorrowRecord savedRecord = borrowRecordRepository.save(record);
        notificationService.sendBorrowingConfirmation(savedRecord);
        return savedRecord;
    }

    @Transactional
    public BorrowRecord returnBook(Long recordId) {
        BorrowRecord record = borrowRecordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("BorrowRecord not found with id: " + recordId));

        if (record.getStatus() == BorrowStatus.RETURNED) {
            throw new BadRequestException("Book is already returned");
        }

        record.setStatus(BorrowStatus.RETURNED);
        record.setActualReturnDate(LocalDate.now());

        Book book = record.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookService.updateBook(book.getId(), book);

        // Check for reservations
        List<Reservation> pendingReservations = reservationRepository.findByBookAndStatusOrderByReservationDateAsc(book, ReservationStatus.PENDING);
        if (!pendingReservations.isEmpty()) {
            Reservation firstReservation = pendingReservations.get(0);
            notificationService.sendReservationReadyNotification(firstReservation);
            // We keep it PENDING or mark as FULFILLED? 
            // Usually it's marked FULFILLED once they actually borrow it, or we could have a READY status.
            // For now, let's keep it PENDING but we've notified them.
            // Actually, let's mark it FULFILLED for simplicity of this flow or just notify.
        }

        return borrowRecordRepository.save(record);
    }

    public List<BorrowRecord> getBorrowHistory(Long userId) {
        User user = userService.getUserById(userId);
        return borrowRecordRepository.findByUserOrderByBorrowDateDesc(user);
    }

    @Transactional
    public BorrowRecord adjustReturnDate(Long recordId, LocalDate newReturnDate) {
        BorrowRecord record = borrowRecordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("BorrowRecord not found with id: " + recordId));

        if (record.getStatus() != BorrowStatus.BORROWED) {
            throw new BadRequestException("Cannot adjust return date for a record that is not currently BORROWED");
        }
        if (newReturnDate.isBefore(LocalDate.now()) || newReturnDate.isEqual(LocalDate.now())) {
            throw new BadRequestException("New return date must be a future date");
        }
        LocalDate maxAllowedDate = record.getBorrowDate().plusDays(30);
        if (newReturnDate.isAfter(maxAllowedDate)) {
            throw new BadRequestException("Return date cannot exceed 30 days from the original borrow date (" + maxAllowedDate + ")");
        }

        record.setExpectedReturnDate(newReturnDate);
        return borrowRecordRepository.save(record);
    }

    public List<BorrowRecord> getAllBorrowRecords() {
        return borrowRecordRepository.findAll();
    }
}
