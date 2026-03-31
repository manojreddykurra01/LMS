package com.library.repository;

import com.library.model.BorrowRecord;
import com.library.model.BorrowStatus;
import com.library.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {
    List<BorrowRecord> findByUserOrderByBorrowDateDesc(User user);
    List<BorrowRecord> findByStatus(BorrowStatus status);
    
    List<BorrowRecord> findByStatusAndExpectedReturnDate(BorrowStatus status, LocalDate expectedReturnDate);
    List<BorrowRecord> findByStatusAndExpectedReturnDateBefore(BorrowStatus status, LocalDate date);
    
    long countByUserAndStatusIn(User user, java.util.Collection<BorrowStatus> statuses);
}
