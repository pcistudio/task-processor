// src/main/java/com/contact/manager/controllers/PositionController.java
package com.contact.manager.controllers;

import com.contact.manager.entities.Candidate;
import com.contact.manager.entities.Position;
import com.contact.manager.model.*;
import com.contact.manager.services.PositionService;
import com.contact.manager.services.scheduler.ScheduleMeeting;
import com.contact.manager.services.scheduler.SchedulerContext;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/positions")
public class PositionController {

    private final PositionService positionService;

    public PositionController(PositionService positionService) {
        this.positionService = positionService;
    }

    @PostMapping
    public ResponseEntity<PositionView> createPosition(@RequestBody Position position) {
        Position createdPosition = positionService.createPosition(position);
        return ResponseEntity.ok(PositionView.from(createdPosition));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PositionView> getPositionById(@PathVariable Long id) {
        Position position = positionService.getPositionById(id);
        if (position != null) {
            return ResponseEntity.ok(PositionView.from(position));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<PositionView> updatePosition(@PathVariable Long id, @RequestBody Position position) {
        try {
            Position updatedPosition = positionService.updatePosition(id, position);
            return ResponseEntity.ok(PositionView.from(updatedPosition));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePosition(@PathVariable Long id) {
        boolean deleted = positionService.deletePosition(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}:sendEmails")
    public ResponseEntity<Void> sendEmailsToCandidates(@PathVariable Long id,
                                                       @RequestBody SendEmailsByPositionRequest request) {
        if (!request.getCandidates().isEmpty()) {
            positionService.sendEmailsToCandidates(id, request.getCandidates(), request.getSubject(), request.getTemplateName());
        } else if (request.getMarkForInterview() != null && request.getMarkForInterview()) {
            positionService.sendEmailsToCandidatesMarkForInterview(id, request.getSubject(), request.getTemplateName());
        } else {
            positionService.sendEmailsToCandidates(id, request.getSubject(), request.getTemplateName());
        }

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}:scheduleInterview")
    public ResponseEntity<List<MeetingResponse>> scheduleInterview(@PathVariable Long id,
                                                                   @RequestBody ScheduleInterviewRequest request) {
        SchedulerContext context = new SchedulerContext.Builder()
//                .startTime(LocalTime.parse(request.getStartTime()))
//                .endTime(LocalTime.parse(request.getEndTime()))
//                .startDate(LocalDate.parse(request.getStartDate()))
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .startDate(request.getStartDate())
                .interviewDurationMinutes(request.getInterviewDurationMinutes())
                .timeBetweenInterviews(request.getTimeBetweenInterviews())
                .sendEmails(request.getSendEmails())
                .build();

        List<ScheduleMeeting> scheduleMeetings = positionService.scheduleInterview(id, request.getSubject(), request.getTemplateName(), context);

        List<MeetingResponse> meetingResponses = MeetingResponse.createMeetingResponses(scheduleMeetings);

        return ResponseEntity.ok(meetingResponses);

    }

    @GetMapping
    public ResponseEntity<List<PositionView>> getAllPositions(@RequestParam(required = false) String filter) {

        List<Position> positions = StringUtils.hasLength(filter) ? positionService.searchPositions(filter) : positionService.getAllPositions();
        List<PositionView> positionViews = positions.stream()
                .map(PositionView::from)
                .toList();

        return ResponseEntity.ok(positionViews);
    }


}