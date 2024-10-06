package com.contact.manager.services;

import com.contact.manager.entities.Contact;
import com.contact.manager.repositories.ContactRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContactService {

    private final ContactRepository contactRepository;


    public ContactService(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    public Contact saveContact(Contact contact) {
        return contactRepository.save(contact);
    }

    public Contact getContactById(Long id) {
        return contactRepository.findById(id).orElse(null);
    }

    public List<Contact> searchContact(String filter) {
        return contactRepository.findByFirstNameContainingOrLastNameContaining(filter, filter);
    }

    public List<Contact> getAllContacts() {
        return contactRepository.findAll();
    }

    public Contact updateContact(Long id, Contact contactDetails) {
        contactDetails.setId(id);
        contactDetails.getAttachments().forEach(attachment -> attachment.setContact(contactDetails));
        contactDetails.getNotes().forEach(note -> note.setContact(contactDetails));
        return contactRepository.save(contactDetails);
    }

    public void deleteContact(Long id) {
        contactRepository.deleteById(id);
    }

}
