// src/main/java/com/contact/manager/model/MeetingResponse.java
package com.contact.manager.model;

import com.contact.manager.services.scheduler.ScheduleMeeting;

import java.time.LocalDateTime;
import java.util.List;

public class MeetingResponse {
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Long candidateId;
    private String firstName;
    private String lastName;

    // Getters and setters
    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public Long getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(Long candidateId) {
        this.candidateId = candidateId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public static List<MeetingResponse> createMeetingResponses(List<ScheduleMeeting> scheduleMeetings) {
        return scheduleMeetings.stream()
                .map(MeetingResponse::createMeetingResponse)
                .toList();
    }

    public static MeetingResponse createMeetingResponse(ScheduleMeeting meeting) {
        MeetingResponse response = new MeetingResponse();
        response.setStartDate(meeting.getStartDate());
        response.setEndDate(meeting.getEndDate());
        response.setCandidateId(meeting.getPerson().getId());
        response.setFirstName(meeting.getPerson().getFirstName());
        response.setLastName(meeting.getPerson().getLastName());
        return response;
    }
}