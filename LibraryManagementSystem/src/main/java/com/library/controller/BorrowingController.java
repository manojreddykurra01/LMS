package com.library.controller;

import com.library.model.BorrowRecord;
import com.library.service.BorrowingService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/borrowings")
@RequiredArgsConstructor
public class BorrowingController {

    private final BorrowingService borrowingService;

    @PostMapping("/borrow")
    @PreAuthorize("hasRole('MEMBER') or hasRole('ADMIN')")
    public ResponseEntity<BorrowRecord> borrowBook(@RequestParam Long userId, @RequestParam Long bookId) {
        return ResponseEntity.ok(borrowingService.borrowBook(userId, bookId));
    }

    @PostMapping("/return/{recordId}")
    @PreAuthorize("hasRole('MEMBER') or hasRole('ADMIN')")
    public ResponseEntity<BorrowRecord> returnBook(@PathVariable Long recordId) {
        return ResponseEntity.ok(borrowingService.returnBook(recordId));
    }

    @PatchMapping("/{recordId}/adjust-return-date")
    @PreAuthorize("hasRole('MEMBER') or hasRole('ADMIN')")
    public ResponseEntity<BorrowRecord> adjustReturnDate(
            @PathVariable Long recordId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate newReturnDate) {
        return ResponseEntity.ok(borrowingService.adjustReturnDate(recordId, newReturnDate));
    }

    @GetMapping("/history/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isOwner(#userId)")
    public ResponseEntity<List<BorrowRecord>> getBorrowHistory(@PathVariable Long userId) {
        return ResponseEntity.ok(borrowingService.getBorrowHistory(userId));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BorrowRecord>> getAllBorrowRecords() {
        return ResponseEntity.ok(borrowingService.getAllBorrowRecords());
    }
}
