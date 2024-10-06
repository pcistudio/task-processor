package com.contact.manager.entities.converter;

import com.contact.manager.entities.Address;
import com.contact.manager.entities.Attachment;
import com.contact.manager.entities.Note;

public class JsonConverters {
    private JsonConverters() {
    }

    public static class AddressJsonConverter extends JsonConverter<Address> {
    }

    public static class AttachmentSetJsonConverter extends SetJsonConverter<Attachment> {
    }

    public static class NoteSetJsonConverter extends SetJsonConverter<Note> {
    }

    public static class NoteListJsonConverter extends ListJsonConverter<Note> {
    }

    public static class AttachmentListJsonConverter extends ListJsonConverter<Attachment> {
    }

    public static class StringListJsonConverter extends ListJsonConverter<String> {
    }


}
