package com.contact.manager.entities;

import java.util.List;
import java.util.Set;

public interface Person {
    String getFirstName();

    String getLastName();

    String getEmail();

    Long getId();

    List<Note> getNotes();

    default boolean anonymous() {
        return false;
    }

    class Anonymous implements Person {
        private final String email;

        private Anonymous(String email) {
            this.email = email;
        }

        public static Anonymous of(String email) {
            return new Anonymous(email);
        }

        @Override
        public String getFirstName() {
            return "";
        }

        @Override
        public String getLastName() {
            return "";
        }

        @Override
        public String getEmail() {
            return email;
        }

        @Override
        public Long getId() {
            return null;
        }

        @Override
        public List<Note> getNotes() {
            return List.of();
        }

        @Override
        public boolean anonymous() {
            return true;
        }
    }
}