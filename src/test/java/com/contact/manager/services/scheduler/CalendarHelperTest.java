package com.contact.manager.services.scheduler;

import com.contact.manager.entities.Candidate;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
class CalendarHelperTest {

    @org.junit.jupiter.api.Test
    void createCalendarFile() {
        CalendarHelper calendarHelper = new CalendarHelper();

        ScheduleMeeting scheduleMeeting = ScheduleMeeting.builder()
                .organizer("organizer@gmail.com")
                .subject("subject")
                .templateName("templateName")
                .person(new Candidate().setEmail("email@gmail.com").setFirstName("name"))
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now())
                .build();
        String calendar = calendarHelper.getCalendarDefinition(scheduleMeeting);
        log.info(calendar);
        assertNotNull(calendar);
    }

}