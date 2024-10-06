package com.contact.manager.entities;

import java.util.ArrayList;
import java.util.List;

public interface Person extends SimplePerson {
    List<Note> getNotes();

    default boolean anonymous() {
        return false;
    }

//    static SimplePerson toSimplePerson(Person person) {
//        return new SimplePerson(person);
//    }
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

//    class SimplePerson implements Person {
//        private final Long id;
//        private final String firstName;
//        private final String lastName;
//        private final String email;
//        private final List<Note> notes;
//
//        public SimplePerson(Long id, String firstName, String lastName, String email, List<Note> notes) {
//            this.id = id;
//            this.firstName = firstName;
//            this.lastName = lastName;
//            this.email = email;
//            this.notes = notes;
//        }
//
//        public SimplePerson(Person person) {
//            this.id = person.getId();
//            this.firstName = person.getFirstName();
//            this.lastName = person.getLastName();
//            this.email = person.getEmail();
//            this.notes = new ArrayList<>(person.getNotes());
//        }
//
//        @Override
//        public String getFirstName() {
//            return firstName;
//        }
//
//        @Override
//        public String getLastName() {
//            return lastName;
//        }
//
//        @Override
//        public String getEmail() {
//            return email;
//        }
//
//        @Override
//        public Long getId() {
//            return id;
//        }
//
//        @Override
//        public List<Note> getNotes() {
//            return notes;
//        }
//    }
}