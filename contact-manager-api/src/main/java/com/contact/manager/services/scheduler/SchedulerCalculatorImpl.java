// src/main/java/com/contact/manager/util/SchedulerManager.java
package com.contact.manager.services.scheduler;

import com.contact.manager.entities.Person;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class SchedulerCalculatorImpl implements SchedulerCalculator {

    public List<ScheduleMeeting> calculateMeetingTimeSlots(MeetingInfo meetingInfo, SchedulerContext context) {
        List<ScheduleMeeting> scheduledMeetings = new ArrayList<>();
        LocalDate currentDate = context.getStartDate();
        LocalTime currentTime = context.getStartTime();
        int interviewDuration = context.getMeetingDurationMinutes();
        int timeBetweenInterviews = context.getTimeBetweenMeetings();


        for (Person person : meetingInfo.getPersons()) {
            if (currentTime.plusMinutes(interviewDuration).isAfter(context.getEndTime())) {
                currentDate = currentDate.plusDays(1);
                currentTime = context.getStartTime();
            }

            LocalDateTime startDateTime = LocalDateTime.of(currentDate, currentTime);
            LocalDateTime endDateTime = startDateTime.plusMinutes(interviewDuration);

            ScheduleMeeting meeting = buildScheduleMeeting(meetingInfo, person, LocalDateTimeRange.of(startDateTime, endDateTime));

            scheduledMeetings.add(meeting);

            currentTime = currentTime.plusMinutes((long)interviewDuration + timeBetweenInterviews);
        }

        return scheduledMeetings;
    }

    @Override
    public ScheduleMeeting buildScheduleMeeting(MeetingInfo meetingInfo, Person person, LocalDateTimeRange localDateTimeRange) {
        return ScheduleMeeting.builder()
                .subject(meetingInfo.getSubject())
                .positionTitle(meetingInfo.getPosition().getTitle())
                .organizer(meetingInfo.getOrganizer())
                .templateName(meetingInfo.getTemplateName())
                .person(person)
                .startDate(localDateTimeRange.getStartDateTime())
                .endDate(localDateTimeRange.getEndDateTime())
                .build();
    }
}