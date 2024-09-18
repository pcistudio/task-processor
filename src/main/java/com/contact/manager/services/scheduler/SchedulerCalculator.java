package com.contact.manager.services.scheduler;

import com.contact.manager.entities.Person;

import java.time.LocalDateTime;
import java.util.List;

public interface SchedulerCalculator {
    List<ScheduleMeeting> calculateMeetingTimeSlots(MeetingInfo meetingInfo, SchedulerContext context);

    ScheduleMeeting buildScheduleMeeting(MeetingInfo meetingInfo, Person person, LocalDateTimeRange localDateTimeRange);
}
