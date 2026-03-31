package com.library.config;

import com.twilio.Twilio;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TwilioConfig {

    private static final Logger logger = LoggerFactory.getLogger(TwilioConfig.class);

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @PostConstruct
    public void initTwilio() {
        if (!"your_account_sid".equals(accountSid) && !accountSid.isEmpty()) {
            Twilio.init(accountSid, authToken);
            logger.info("Twilio initialized successfully");
        } else {
            logger.warn("Twilio credentials not configured properly");
        }
    }
}
