package com.library.controller;

import com.library.service.NotificationScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationScheduler notificationScheduler;

    @GetMapping("/trigger-reminders")
    @PreAuthorize("hasRole('ADMIN')")
    public String triggerReminders() {
        notificationScheduler.processDueSoonReminders();
        notificationScheduler.processOverdueReminders();
        return "Manual reminder trigger completed. Check application logs for details.";
    }
}
