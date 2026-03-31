package com.library.controller;

import com.library.model.Book;
import com.library.model.Feedback;
import com.library.model.User;
import com.library.repository.FeedbackRepository;
import com.library.service.BookService;
import com.library.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackRepository feedbackRepository;
    private final BookService bookService;
    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('MEMBER') or hasRole('ADMIN')")
    public ResponseEntity<Feedback> addFeedback(@RequestParam Long userId, @RequestParam Long bookId, @RequestBody Feedback feedbackRequest) {
        User user = userService.getUserById(userId);
        Book book = bookService.getBookById(bookId);

        Feedback feedback = Feedback.builder()
                .user(user)
                .book(book)
                .rating(feedbackRequest.getRating())
                .comments(feedbackRequest.getComments())
                .build();

        return ResponseEntity.ok(feedbackRepository.save(feedback));
    }

    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<Feedback>> getFeedbacksForBook(@PathVariable Long bookId) {
        Book book = bookService.getBookById(bookId);
        return ResponseEntity.ok(feedbackRepository.findByBook(book));
    }
}
