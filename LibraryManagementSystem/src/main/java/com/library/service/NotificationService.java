package com.library.service;

import com.library.model.BorrowRecord;
import com.library.model.Reservation;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final JavaMailSender javaMailSender;

    @Value("${twilio.phone-number}")
    private String twilioPhoneNumber;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            javaMailSender.send(message);
            logger.info("Email sent to: " + to);
        } catch (Exception e) {
            logger.error("Failed to send email to " + to, e);
        }
    }

    public void sendSms(String toPhoneNumber, String text) {
        try {
            if ("your_phone_number".equals(twilioPhoneNumber) || twilioPhoneNumber.isEmpty()) {
                logger.warn("Simulated SMS to " + toPhoneNumber + ": " + text);
                return;
            }
            Message message = Message.creator(
                    new PhoneNumber(toPhoneNumber),
                    new PhoneNumber(twilioPhoneNumber),
                    text
            ).create();
            logger.info("SMS sent successfully: " + message.getSid());
        } catch (Exception e) {
            logger.error("Failed to send SMS to " + toPhoneNumber, e);
        }
    }

    public void sendDueSoonReminder(BorrowRecord record) {
        String to = record.getUser().getEmail();
        String subject = "[LMS Library] Reminder: Your book is due soon";
        String text = String.format("Dear %s,\n\nThis is a friendly reminder from the Library Management System that the following book is due on %s:\n\n" +
                        "- Title: %s\n- Author: %s\n- ISBN: %s\n\n" +
                        "Please return it to the library by the due date to avoid any late fees.\n\nThank you!\nLMS Library Team",
                record.getUser().getUsername(), record.getExpectedReturnDate(), 
                record.getBook().getTitle(), record.getBook().getAuthor(), record.getBook().getIsbn());
        sendEmail(to, subject, text);
    }

    public void sendOverdueReminder(BorrowRecord record) {
        String to = record.getUser().getEmail();
        String subject = "[LMS Library] URGENT: Book Overdue";
        String text = String.format("Dear %s,\n\nThe following book was due on %s and is now OVERDUE:\n\n" +
                        "- Title: %s\n- Author: %s\n- ISBN: %s\n\n" +
                        "Please return it as soon as possible to avoid further late fees.\n\nThank you!\nLMS Library Team",
                record.getUser().getUsername(), record.getExpectedReturnDate(),
                record.getBook().getTitle(), record.getBook().getAuthor(), record.getBook().getIsbn());
        sendEmail(to, subject, text);
    }

    public void sendReservationReadyNotification(Reservation reservation) {
        String to = reservation.getUser().getEmail();
        String subject = "[LMS Library] Reserved Book Available";
        String text = String.format("Dear %s,\n\nGreat news! The book you reserved is now available for pickup:\n\n" +
                        "- Title: %s\n- Author: %s\n- ISBN: %s\n\n" +
                        "Please collect it within the next 3 days.\n\nHappy reading!\nLMS Library Team",
                reservation.getUser().getUsername(), reservation.getBook().getTitle(),
                reservation.getBook().getAuthor(), reservation.getBook().getIsbn());
        sendEmail(to, subject, text);
    }

    public void sendGreetingEmail(com.library.model.User user) {
        String to = user.getEmail();
        String subject = "Welcome to LMS Library - Your Account is Ready!";
        String text = String.format("Dear %s,\n\nWelcome to the Library Management System! We're excited to have you as a member.\n\n" +
                        "Your account has been successfully created. You can now explore our extensive collection of books, reserve your favorite titles, and manage your borrowings online.\n\n" +
                        "If you have any questions or need assistance, feel free to contact our support team.\n\nHappy reading!\nLMS Library Team",
                user.getUsername());
        sendEmail(to, subject, text);
    }

    public void sendBorrowingConfirmation(BorrowRecord record) {
        String to = record.getUser().getEmail();
        String subject = "[LMS Library] Borrowing Confirmation: " + record.getBook().getTitle();
        String text = String.format("Dear %s,\n\nThis is to confirm that you have borrowed the following book:\n\n" +
                        "- Title: %s\n- Author: %s\n- Borrow Date: %s\n- Due Date: %s\n\n" +
                        "Please ensure the book is returned by the due date to avoid any late fees.\n\n" +
                        "Thank you for using our library!\nLMS Library Team",
                record.getUser().getUsername(), record.getBook().getTitle(),
                record.getBook().getAuthor(), record.getBorrowDate(), record.getExpectedReturnDate());
        sendEmail(to, subject, text);
    }
}
