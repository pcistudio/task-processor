// src/main/java/com/contact/manager/repositories/PositionRepository.java
package com.contact.manager.repositories;

import com.contact.manager.entities.Position;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PositionRepository extends JpaRepository<Position, Long> {
    List<Position> findByTitleContainingAndDescriptionContaining(String filter, String filter1);
}