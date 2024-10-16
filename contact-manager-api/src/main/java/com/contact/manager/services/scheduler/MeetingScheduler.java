package com.contact.manager.services.scheduler;

import com.contact.manager.services.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;

import static com.contact.manager.util.TemplateHelper.convertToTemplateParams;

@Slf4j
@Component
public class MeetingScheduler {
    private final SchedulerCalculator schedulerCalculator;
    private final CalendarHelper calendarHelper;
    private final MailService mailService;
    private final String organizer;

    public MeetingScheduler(SchedulerCalculator schedulerCalculator, CalendarHelper calendarHelper, MailService mailService,
                            @Value("spring.mail.username") String organizer) {
        this.schedulerCalculator = schedulerCalculator;
        this.calendarHelper = calendarHelper;
        this.mailService = mailService;
        this.organizer = organizer;
    }

    public List<ScheduleMeeting> scheduleMeetings(MeetingInfo.MeetingInfoBuilder meetingInfoBuilder, SchedulerContext context) {
        MeetingInfo meetingInfo = createMeetingInfo(meetingInfoBuilder);
        List<ScheduleMeeting> scheduleMeetings = schedulerCalculator.calculateMeetingTimeSlots(meetingInfo, context);
        if (context.shouldSendEmails()) {
            sendMeetings(scheduleMeetings);
        }
        return scheduleMeetings;
    }

    private MeetingInfo createMeetingInfo(MeetingInfo.MeetingInfoBuilder meetingInfoBuilder) {
        initMeetingInfo(meetingInfoBuilder);
        return meetingInfoBuilder.build();
    }

    protected void initMeetingInfo(MeetingInfo.MeetingInfoBuilder meetingInfoBuilder) {
        meetingInfoBuilder.organizer(organizer);
    }

    public void sendMeetings(List<ScheduleMeeting> scheduleMeetings) {
        for (ScheduleMeeting scheduleMeeting : scheduleMeetings) {
            sendMeeting(scheduleMeeting);
        }
    }

    public ScheduleMeeting scheduleMeeting(MeetingInfo.MeetingInfoBuilder meetingInfoBuilder, LocalDateTimeRange localDateTimeRange) {
        MeetingInfo meetingInfo = createMeetingInfo(meetingInfoBuilder);
        Assert.isTrue(meetingInfo.getPersons().size() == 1, "Only one person can be scheduled");
        ScheduleMeeting scheduleMeeting = schedulerCalculator.buildScheduleMeeting(meetingInfo, meetingInfo.getPersons().get(0), localDateTimeRange);
        sendMeeting(scheduleMeeting);
        return scheduleMeeting;
    }

    public void sendMeeting(ScheduleMeeting scheduleMeeting) {
        try {

            Map<String, Object> templateParams = convertToTemplateParams(
                    scheduleMeeting.getPerson(),
                    Map.of("startDate", scheduleMeeting.getStartDate(), "endDate", scheduleMeeting.getEndDate(),
                            "meeting.ics", calendarHelper.createCalendarFile(scheduleMeeting),
                            "positionTitle", scheduleMeeting.getPositionTitle()
                    )
            );
            mailService.sendEmailWithTemplate(scheduleMeeting.getPerson(), scheduleMeeting.getSubject(), scheduleMeeting.getTemplateName(), templateParams);
        } catch (Exception e) {
            log.error("Error sending email to {}", scheduleMeeting.getPerson().getEmail(), e);
        }
    }
}
