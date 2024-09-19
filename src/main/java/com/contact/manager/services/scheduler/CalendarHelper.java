package com.contact.manager.services.scheduler;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Cn;
import net.fortuna.ical4j.model.parameter.Role;
import net.fortuna.ical4j.model.property.*;
import net.fortuna.ical4j.util.RandomUidGenerator;
import net.fortuna.ical4j.util.UidGenerator;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URI;
import java.time.ZoneId;
import java.util.Date;


@Component
public class CalendarHelper {

    public String getCalendarDefinition(ScheduleMeeting scheduleMeeting) {
        Date startTime = Date.from(scheduleMeeting.getStartDate().atZone(ZoneId.systemDefault()).toInstant());
        Date to = Date.from(scheduleMeeting.getEndDate().atZone(ZoneId.systemDefault()).toInstant());

        Calendar calendar = new Calendar();
        calendar.getProperties().add(new ProdId("-//Contact Manager//iCal4j 1.0//EN"));
        calendar.getProperties().add(Version.VERSION_2_0);
        calendar.getProperties().add(CalScale.GREGORIAN);

        VEvent meeting = new VEvent(new net.fortuna.ical4j.model.DateTime(startTime), new net.fortuna.ical4j.model.DateTime(to), scheduleMeeting.getSubject());
        UidGenerator ug = new RandomUidGenerator();
        meeting.getProperties().add(ug.generateUid());

        // Add organizer (optional)
        Organizer organizer = new Organizer(URI.create("mailto:" + scheduleMeeting.getOrganizer()));
        meeting.getProperties().add(organizer);

        // Add an attendee (optional)
        Attendee attendee = new Attendee(URI.create("mailto:" + scheduleMeeting.getPerson().getEmail()));
        attendee.getParameters().add(Role.REQ_PARTICIPANT);
        attendee.getParameters().add(new Cn(scheduleMeeting.getPerson().getFirstName()));
        meeting.getProperties().add(attendee);

        calendar.getComponents().add(meeting);

        return calendar.toString();
    }

    public InputStream createCalendarInputStream(ScheduleMeeting scheduleMeeting) {
        String calendarDefinition = getCalendarDefinition(scheduleMeeting);
        return new ByteArrayInputStream(calendarDefinition.getBytes());
    }

    public File createCalendarFile(ScheduleMeeting scheduleMeeting) throws IOException {
        String calendarDefinition = getCalendarDefinition(scheduleMeeting);

        File icsFile = File.createTempFile("meeting", ".ics");
        try (FileOutputStream fout = new FileOutputStream(icsFile)) {
            fout.write(calendarDefinition.getBytes());
        }
        return icsFile;
    }


}
