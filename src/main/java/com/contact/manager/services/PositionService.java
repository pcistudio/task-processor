package com.contact.manager.services;

import com.contact.manager.entities.Position;
import com.contact.manager.services.scheduler.ScheduleMeeting;
import com.contact.manager.services.scheduler.SchedulerContext;

import java.util.List;

public interface PositionService {
    Position createPosition(Position position);
    Position getPositionById(Long id);
    Position updatePosition(Long id, Position position);
    boolean deletePosition(Long id);
    void sendEmailsToCandidates(Long id, String subject, String templateName);

    void sendEmailsToCandidatesMarkForInterview(Long id, String subject, String templateName);

    void sendEmailsToCandidates(Long positionId, List<Long> candidates, String subject, String templateName);

    List<ScheduleMeeting> scheduleInterview(Long positionId, String subject, String templateName, SchedulerContext context);

    List<Position> searchPositions(String filter);

    List<Position> getAllPositions();
}