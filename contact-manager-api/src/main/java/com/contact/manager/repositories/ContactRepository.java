package com.contact.manager.repositories;

import com.contact.manager.entities.Contact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContactRepository extends JpaRepository<Contact, Long> {
    List<Contact> findByFirstNameContainingOrLastNameContaining(String filter, String filter1);
}



