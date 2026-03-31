package com.library.service;

import com.library.model.BorrowRecord;
import com.library.model.BorrowStatus;
import com.library.repository.BorrowRecordRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private static final Logger logger = LoggerFactory.getLogger(NotificationScheduler.class);

    private final BorrowRecordRepository borrowRecordRepository;
    private final NotificationService notificationService;

    @Value("${library.notification.due-soon-days:2}")
    private List<Integer> dueSoonDays;

    /**
     * Runs every day at 8:00 AM to send reminders for books due in configured days (e.g., 2, 1).
     * Cron format: second, minute, hour, day of month, month, day(s) of week
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void processDueSoonReminders() {
        logger.info("Starting scheduled task: Due Soon Reminders for days: {}", dueSoonDays);
        
        for (int days : dueSoonDays) {
            LocalDate targetDate = LocalDate.now().plusDays(days);
            List<BorrowRecord> dueSoonRecords = borrowRecordRepository.findByStatusAndExpectedReturnDate(BorrowStatus.BORROWED, targetDate);
            
            for (BorrowRecord record : dueSoonRecords) {
                notificationService.sendDueSoonReminder(record);
            }
            logger.info("Sent {} notifications for books due in {} days.", dueSoonRecords.size(), days);
        }
        logger.info("Finished scheduled task: Due Soon Reminders");
    }

    /**
     * Runs every day at 9:00 AM to send reminders for overdue books.
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void processOverdueReminders() {
        logger.info("Starting scheduled task: Overdue Reminders");
        List<BorrowRecord> overdueRecords = borrowRecordRepository.findByStatusAndExpectedReturnDateBefore(BorrowStatus.BORROWED, LocalDate.now());
        
        for (BorrowRecord record : overdueRecords) {
            notificationService.sendOverdueReminder(record);
        }
        logger.info("Finished scheduled task: Overdue Reminders. Sent {} notifications.", overdueRecords.size());
    }
}
