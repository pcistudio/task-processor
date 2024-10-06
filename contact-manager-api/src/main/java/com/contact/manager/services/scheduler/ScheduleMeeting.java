package com.contact.manager.services.scheduler;

import com.contact.manager.entities.Person;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class ScheduleMeeting {
    @NotNull
    private String organizer;
    @NotNull
    private String positionTitle;
    @NotNull
    private String subject;
    @NotNull
    private String templateName;
    @NotNull
    private Person person;
    @NotNull
    private LocalDateTime startDate;
    @NotNull
    private LocalDateTime endDate;

}
