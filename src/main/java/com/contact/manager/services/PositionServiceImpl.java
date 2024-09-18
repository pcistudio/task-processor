package com.contact.manager.services;

import com.contact.manager.entities.Candidate;
import com.contact.manager.entities.Position;
import com.contact.manager.repositories.CandidateRepository;
import com.contact.manager.repositories.PositionRepository;
import com.contact.manager.services.scheduler.MeetingInfo;
import com.contact.manager.services.scheduler.MeetingScheduler;
import com.contact.manager.services.scheduler.ScheduleMeeting;
import com.contact.manager.services.scheduler.SchedulerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
public class PositionServiceImpl implements PositionService {

    private final PositionRepository positionRepository;
    private final CandidateRepository candidateRepository;
    private final MeetingScheduler meetingScheduler;
    private final MailService mailService;

    public PositionServiceImpl(PositionRepository positionRepository, CandidateRepository candidateRepository, MeetingScheduler meetingScheduler, MailService mailService) {
        this.positionRepository = positionRepository;
        this.candidateRepository = candidateRepository;
        this.meetingScheduler = meetingScheduler;
        this.mailService = mailService;
    }

    @Override
    public Position createPosition(Position position) {
        Assert.isNull(position.getId(), "Position id must be null");
        return positionRepository.save(position);

    }

    @Override
    public Position getPositionById(Long positionId) {
        Optional<Position> position = positionRepository.findById(positionId);
        return position.orElse(null);
    }

    @Override
    public Position updatePosition(Long positionId, Position position) {
        Position positionStored = positionRepository.findById(positionId)
                .orElseThrow(() -> new IllegalArgumentException("Position not found"));

        position.setId(positionId);
        return positionRepository.save(position);
    }

    @Override
    public boolean deletePosition(Long positionId) {
        if (positionRepository.existsById(positionId)) {
            positionRepository.deleteById(positionId);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void sendEmailsToCandidates(Long positionId, String subject, String templateName) {

        log.debug("Sending emails to candidates for positionId={}", positionId);
        Position position = positionRepository.findById(positionId)
                .orElseThrow(() -> new IllegalArgumentException("Position not found"));

        List<Candidate> candidates = position.getCandidates();
        mailService.sendEmailsToPersons(candidates, subject, templateName);
    }

    @Override
    public void sendEmailsToCandidatesMarkForInterview(Long positionId, String subject, String templateName) {
        log.debug("Sending emails to candidates mark for interview for positionId={}", positionId);
        List<Candidate> candidates = getCandidatesMarkForInterview(positionId);

        mailService.sendEmailsToPersons(candidates, subject, templateName);
    }

    @Override
    public void sendEmailsToCandidates(Long positionId, List<Long> candidatesId, String subject, String templateName) {
        log.debug("Sending emails to candidates ids={} for positionId={}", candidatesId, positionId);
        List<Candidate> candidates = candidateRepository.findByIdInAndPositionId(candidatesId, positionId);

        mailService.sendEmailsToPersons(candidates, subject, templateName);
    }

    private List<Candidate> getCandidatesMarkForInterview(Long positionId) {

        //FIXME: This is not working. Posible solution is to use a custom query @Query
        List<Candidate> candidates = candidateRepository.findByMarkForInterviewAndPositionId(true, positionId);
        return candidates;
    }

    @Override
    public List<ScheduleMeeting> scheduleInterview(Long positionId, String subject, String templateName, SchedulerContext context) {
        // Existing logic to get candidates and mark them for interview
        Position position = positionRepository.findById(positionId)
                .orElseThrow(() -> new IllegalArgumentException("Position not found"));
        List<Candidate> candidates = getCandidatesMarkForInterview(positionId);
        if (candidates.isEmpty()) {
            log.warn("No candidates found for interview positionId={}", positionId);
            return List.of();
        }
        // Create MeetingInfo
        MeetingInfo.MeetingInfoBuilder meetingInfoBuilder = MeetingInfo.builder()
                .persons(candidates)
                .position(position)
                .subject(Objects.requireNonNullElseGet(subject, () -> getPositionTitle(positionId) + " Interview"))
                .templateName(templateName)
                ;


        // Schedule meetings
        return meetingScheduler.scheduleMeetings(meetingInfoBuilder, context);
    }

    @Override
    public List<Position> searchPositions(String filter) {
        return positionRepository.findByTitleContainingAndDescriptionContaining(filter, filter);
    }

    @Override
    public List<Position> getAllPositions() {
        return positionRepository.findAll();
    }

    public String getPositionTitle(Long positionId) {
        Optional<Position> position = positionRepository.findById(positionId);
        return position
                .map(Position::getTitle)
                .orElse(null);
    }
}