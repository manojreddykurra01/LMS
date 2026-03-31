package com.library.service;

import com.library.model.Book;
import com.library.model.User;
import com.library.repository.BookRepository;
import com.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public Map<String, Object> getLibraryStats() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalBooks = bookRepository.count();
        long totalUsers = userRepository.count();
        
        List<Book> books = bookRepository.findAll();
        long unAvailableBooks = books.stream().filter(b -> b.getAvailableCopies() == 0).count();

        stats.put("totalBooks", totalBooks);
        stats.put("totalUsers", totalUsers);
        stats.put("currentlyBorrowedBooks", unAvailableBooks);
        
        return stats;
    }
}
