package com.scalelink.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ClickAnalyticsService {

    private static final Logger logger = LoggerFactory.getLogger(ClickAnalyticsService.class);

    public void processClickEvent(String shortCode) {
        logger.info("Processing click event for shortCode: {}", shortCode);
    }
}
