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
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    private static final Logger log = LoggerFactory.getLogger(ContactController.class);
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
    public ResponseEntity<Contact> createContact(@RequestBody @Valid Contact contact) {
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

        List<Contact> contacts = StringUtils.hasLength(filter) ? contactService.searchContact(filter) : contactService.getAllContacts();
        return ResponseEntity.ok(
                contacts.stream()
                        .map(ContactView::new)
                        .toList()
        );
    }


    @PostMapping("/{id}/notes")
    public ResponseEntity<NoteView> createNote(@PathVariable("id") Long contactId, @RequestBody @Valid NoteRequest note) {
        Note createdNote = noteService.saveNote(contactId, note);
        return ResponseEntity.ok(new NoteView(createdNote));
    }

    // Update Address
    @PutMapping("/{contactId}/addresses/{addressId}")
    public ResponseEntity<Address> updateAddress(@PathVariable Long contactId, @PathVariable Long addressId, @RequestBody @Valid AddressRequest addressRequest) {
        Address updatedAddress = addressService.updateAddress(contactId, addressId, addressRequest);
        return ResponseEntity.ok(updatedAddress);
    }

    // Delete Address
    @DeleteMapping("/{contactId}/addresses/{addressId}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long contactId, @PathVariable Long addressId) {
        addressService.deleteAddress(contactId, addressId);
        return ResponseEntity.noContent().build();
    }

    // Delete Attachment
    @DeleteMapping("/{contactId}/attachments/{attachmentId}")
    public ResponseEntity<Void> deleteAttachment(@PathVariable Long contactId, @PathVariable Long attachmentId) {
        attachmentService.deleteAttachment(attachmentId);
        return ResponseEntity.noContent().build();
    }

    // Update Note
    @PutMapping("/{contactId}/notes/{noteId}")
    public ResponseEntity<NoteView> updateNote(@PathVariable Long contactId, @PathVariable Long noteId, @RequestBody @Valid NoteRequest noteRequest) {
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
    public ResponseEntity<ContactModel> updateContact(@PathVariable Long contactId, @RequestBody @Valid Contact contactDetails) {
        Contact updatedContact = contactService.updateContact(contactId, contactDetails);
        return ResponseEntity.ok(new ContactModel(updatedContact));
    }

    @DeleteMapping("/{contactId}")
    public ResponseEntity<Void> deleteContact(@PathVariable Long contactId) {
        contactService.deleteContact(contactId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{contactId}/attachments/{attachmentId}")
    public ResponseEntity<Resource> serveFile(@PathVariable long contactId, @PathVariable long attachmentId) {
        try {
            AttachmentResource attachmentResource = attachmentService.loadAttachmentAsResource(contactId, attachmentId);
            log.info("Downloading attachment: {}", attachmentResource.getAttachment().getFileName());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachmentResource.getAttachment().getFileName() + "\"")
                    .body(attachmentResource.getResource());
        } catch (IllegalArgumentException ex) {
            log.error("Attachment not found contactId={}, index={}", contactId, attachmentId, ex);
            return ResponseEntity.notFound().build();
        }
    }

// TODO later
//    @PostMapping("/{contactId}/convert-to-employee")
//    public String convertToEmployee(@PathVariable Long contactId,
//                                    @RequestParam BigDecimal salary,
//                                    @RequestParam String paymentFrequency,
//                                    @RequestParam LocalDate startedDate,
//                                    @RequestParam(required = false) LocalDate endDate) {
//        contactService.
//        Contact contact = contactRepository.findById(contactId)
//                .orElseThrow(() -> new IllegalArgumentException("Contact not found"));
//
//        Employee employee = new Employee();
//        employee.setId(contact.getId());
//        employee.setFirstName(contact.getFirstName());
//        employee.setLastName(contact.getLastName());
//        employee.setEmail(contact.getEmail());
//        employee.setJobTitle(contact.getJobTitle());
//        employee.setOfficePhone(contact.getOfficePhone());
//        employee.setMobile(contact.getMobile());
//        employee.setDescription(contact.getDescription());
//        employee.setPrimaryAddress(contact.getPrimaryAddress());
//        employee.setSecondaryAddress(contact.getSecondaryAddress());
//        employee.setNotes(contact.getNotes());
//        employee.setAttachments(contact.getAttachments());
//        employee.setSalary(salary);
//        employee.setPaymentFrequency(PaymentFrequency.valueOf(paymentFrequency));
//        employee.setStartedDate(startedDate);
//        employee.setEndDate(endDate);
//
//        employeeRepository.save(employee);

//        return "Contact converted to Employee with ID: " + employee.getId();
//    }

}
