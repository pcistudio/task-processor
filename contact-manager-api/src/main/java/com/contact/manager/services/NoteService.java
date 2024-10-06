// src/main/java/com/contact/manager/services/NoteService.java
package com.contact.manager.services;

import com.contact.manager.entities.Note;
import com.contact.manager.model.NoteRequest;
import com.contact.manager.repositories.ContactRepository;
import com.contact.manager.repositories.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NoteService {

    private final NoteRepository noteRepository;
    private final ContactRepository contactRepository;

    @Autowired
    public NoteService(NoteRepository noteRepository, ContactRepository contactRepository) {
        this.noteRepository = noteRepository;
        this.contactRepository = contactRepository;
    }

    public Note saveNote(Long contactId, NoteRequest noteRequest) {
        Note note = new Note();
        note.setContact(contactRepository.getReferenceById(contactId));
        note.setContent(noteRequest.getContent());
        return noteRepository.save(note);
    }

    public Note updateNote(Long noteId, NoteRequest noteRequest) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("Note not found"));
        note.setContent(noteRequest.getContent());
        return noteRepository.save(note);
    }

    public void deleteNote(Long noteId) {
        noteRepository.deleteById(noteId);
    }
}