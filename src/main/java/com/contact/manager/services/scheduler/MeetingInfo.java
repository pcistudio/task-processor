package com.contact.manager.services.scheduler;

import com.contact.manager.entities.Person;
import com.contact.manager.entities.Position;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class MeetingInfo {
    private Position position;
    private String organizer;
    private List<? extends Person> persons;
    private String subject;
    private String templateName;
}
