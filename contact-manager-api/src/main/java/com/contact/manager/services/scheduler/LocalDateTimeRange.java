package com.contact.manager.services.scheduler;

import java.time.LocalDateTime;

public interface LocalDateTimeRange {
    LocalDateTime getStartDateTime();
    LocalDateTime getEndDateTime();

    static LocalDateTimeRange of(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return new LocalDateTimeRange() {
            @Override
            public LocalDateTime getStartDateTime() {
                return startDateTime;
            }

            @Override
            public LocalDateTime getEndDateTime() {
                return endDateTime;
            }
        };
    }
}
