// src/main/java/com/contact/manager/repository/CandidateRepository.java
package com.contact.manager.repositories;

import com.contact.manager.entities.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {
    List<Candidate> findByFirstNameContainingOrLastNameContaining(String filter, String filter1);

    List<Candidate> findByIdInAndPositionId(List<Long> candidates, Long positionId);

    List<Candidate> findByMarkForInterviewAndPositionId(boolean markForInterview, Long positionId);
}