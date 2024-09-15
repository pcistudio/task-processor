package com.contact.manager.model;

import com.contact.manager.entities.Note;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Set;

public class NoteView {
    private Long id;
    private String content;
    private Long contactId;

    public NoteView(Note note) {
        Assert.notNull(note, "Note must not be null");
        this.id = note.getId();
        this.content = note.getContent();
        this.contactId = note.getContact().getId();
    }

    public static List<NoteView> fromNotes(Set<Note> notes) {
        if (notes != null) {
            return notes.stream().map(NoteView::new).toList();
        }
        return List.of();
    }

    public Long getId() {
        return id;
    }

    public NoteView setId(Long id) {
        this.id = id;
        return this;
    }

    public String getContent() {
        return content;
    }

    public NoteView setContent(String content) {
        this.content = content;
        return this;
    }

    public Long getContactId() {
        return contactId;
    }

    public NoteView setContactId(Long contactId) {
        this.contactId = contactId;
        return this;
    }
}