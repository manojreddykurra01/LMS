package com.library.service;

import com.library.exception.BadRequestException;
import com.library.exception.ResourceNotFoundException;

import com.library.model.User;
import com.library.model.BorrowStatus;
import com.library.repository.UserRepository;
import com.library.repository.BorrowRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;
    private final BorrowRecordRepository borrowRecordRepository;


    public User registerUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new BadRequestException("Username is already taken");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new BadRequestException("Email is already registered");
        }
        if (userRepository.existsByPhone(user.getPhone())) {
            throw new BadRequestException("Phone number is already registered");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User registeredUser = userRepository.save(user);
        notificationService.sendGreetingEmail(registeredUser);
        return registeredUser;
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @org.springframework.transaction.annotation.Transactional
    public void deleteUser(Long id) {
        User user = getUserById(id);
        
        // Prevent deleting users with active borrowings
        long activeCount = borrowRecordRepository.countByUserAndStatusIn(user, 
                Arrays.asList(BorrowStatus.BORROWED, BorrowStatus.OVERDUE));
        
        if (activeCount > 0) {
            throw new BadRequestException("Cannot delete member who currently has " + activeCount + " borrowed or overdue book(s).");
        }
        
        userRepository.delete(user);
    }
}
