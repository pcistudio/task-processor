package com.contact.manager.controllers;

import com.contact.manager.entities.Address;
import com.contact.manager.entities.Attachment;
import com.contact.manager.entities.Contact;
import com.contact.manager.entities.Note;
import com.contact.manager.model.*;
import com.contact.manager.services.AddressService;
import com.contact.manager.services.AttachmentService;
import com.contact.manager.services.ContactService;
import com.contact.manager.services.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    private final ContactService contactService;
    private final NoteService noteService;
    private final AddressService addressService;
    private final AttachmentService attachmentService;

    public ContactController(ContactService contactService, NoteService noteService, AddressService addressService, AttachmentService attachmentService) {
        this.contactService = contactService;
        this.noteService = noteService;
        this.addressService = addressService;
        this.attachmentService = attachmentService;
    }

    @PostMapping
    public ResponseEntity<Contact> createContact(@RequestBody Contact contact) {
        return ResponseEntity.ok(contactService.saveContact(contact));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContactModel> getContact(@PathVariable Long id) {
        Contact contact = contactService.getContactById(id);

        return ResponseEntity.ok(ContactModel.fromContact(contact));
    }

    @PostMapping("/{id}/attachments")
    public ResponseEntity<AttachmentView> uploadAttachment(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        Attachment attachment = attachmentService.saveAttachment(id, file);
        return ResponseEntity.ok(new AttachmentView(attachment));
    }

    @GetMapping
    public ResponseEntity<List<ContactView>> searchContact(@RequestParam(required = false) String filter) {

        List<Contact> contacts = StringUtils.hasLength(filter)? contactService.searchContact(filter): contactService.getAllContacts();
        return ResponseEntity.ok(
                contacts.stream()
                        .map(ContactView::new)
                        .toList()
        );
    }


    @PostMapping("/{id}/notes")
    public ResponseEntity<NoteView> createNote(@PathVariable("id") Long contactId, @RequestBody NoteRequest note) {
        Note createdNote = noteService.saveNote(contactId, note);
        return ResponseEntity.ok(new NoteView(createdNote));
    }

    // Update Address
    @PutMapping("/{contactId}/addresses/{addressId}")
    public ResponseEntity<Address> updateAddress(@PathVariable Long contactId, @PathVariable Long addressId, @RequestBody AddressRequest addressRequest) {
        Address updatedAddress = addressService.updateAddress(contactId, addressId, addressRequest);
        return ResponseEntity.ok(updatedAddress);
    }

    // Delete Address
    @DeleteMapping("/{contactId}/addresses/{addressId}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long contactId, @PathVariable Long addressId) {
        addressService.deleteAddress(contactId, addressId);
        return ResponseEntity.noContent().build();
    }

    // Update Attachment
    //TODO Add contactId and check if the attachment belongs to the contact
    @PutMapping("/{contactId}/attachments/{attachmentId}")
    public ResponseEntity<AttachmentView> updateAttachment(@PathVariable Long contactId, @PathVariable Long attachmentId, @RequestBody AttachmentRequest attachmentRequest) {
        Attachment updatedAttachment = attachmentService.updateAttachment(attachmentId, attachmentRequest);
        return ResponseEntity.ok(new AttachmentView(updatedAttachment));
    }

    // Delete Attachment
    @DeleteMapping("/{contactId}/attachments/{attachmentId}")
    public ResponseEntity<Void> deleteAttachment(@PathVariable Long contactId, @PathVariable Long attachmentId) {
        attachmentService.deleteAttachment(attachmentId);
        return ResponseEntity.noContent().build();
    }

    // Update Note
    @PutMapping("/{contactId}/notes/{noteId}")
    public ResponseEntity<NoteView> updateNote(@PathVariable Long contactId, @PathVariable Long noteId, @RequestBody NoteRequest noteRequest) {
        Note updatedNote = noteService.updateNote(noteId, noteRequest);
        return ResponseEntity.ok(new NoteView(updatedNote));
    }

    // Delete Note
    @DeleteMapping("/{contactId}/notes/{noteId}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long contactId, @PathVariable Long noteId) {
        noteService.deleteNote(noteId);
        return ResponseEntity.noContent().build();
    }


    @PutMapping("/{contactId}")
    public ResponseEntity<ContactView> updateContact(@PathVariable Long contactId, @RequestBody Contact contactDetails) {
        Contact updatedContact = contactService.updateContact(contactId, contactDetails);
        return ResponseEntity.ok(new ContactView(updatedContact));
    }

    @DeleteMapping("/{contactId}")
    public ResponseEntity<Void> deleteContact(@PathVariable Long contactId) {
        contactService.deleteContact(contactId);
        return ResponseEntity.noContent().build();
    }

}
