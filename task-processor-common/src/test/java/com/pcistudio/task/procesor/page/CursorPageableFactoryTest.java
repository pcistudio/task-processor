package com.pcistudio.task.procesor.page;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@Slf4j
class CursorPageableFactoryTest {

    @Test
    void createPageable() {

        PersonCursorPageableFactory factory = new PersonCursorPageableFactory();

        Pageable<Person> pageable = factory.createPageable(this::getPersons, 5, null);
        Assertions.assertThat(pageable.results()).isNotEmpty();
        log.info("pageToken: {}", pageable.nextPageToken());
        Cursor<String> stringCursor = factory.decodeCursor(pageable.nextPageToken());
        assertEquals("Brown", stringCursor.offset());
        assertEquals(5L, stringCursor.id());
    }

    @Test
    void createPageable1() {

        PersonCursorPageableFactory factory = new PersonCursorPageableFactory();

        Pageable<Person> pageable = factory.createPageable(this::getPersons, 5, null);
        Assertions.assertThat(pageable.results()).isNotEmpty();
        Assertions.assertThat(pageable.results()).hasSize(5);

        Pageable<Person> pageable1 = factory.createPageable(this::getPersons2, 5, pageable.nextPageToken());

        Assertions.assertThat(pageable1.results()).isNotEmpty();
        Assertions.assertThat(pageable1.results()).hasSize(3);
        assertNull(pageable1.nextPageToken());
    }

    public List<Person> getPersons(Cursor<String> cursor, int limit) {

        return List.of(
                new Person(1L, "John", 20),
                new Person(2L, "Jane", 25),
                new Person(3L, "Doe", 30),
                new Person(4L, "Smith", 35),
                new Person(5L, "Brown", 40)
        );
    }

    public List<Person> getPersons2(Cursor<String> cursor, int limit) {
        assertEquals("Brown", cursor.offset());
        assertEquals(5L, cursor.id());
        return List.of(
                new Person(6L, "John", 20),
                new Person(7L, "Jane", 25),
                new Person(8L, "Doe", 30)
        );
    }

    record Person(Long id, String name, int age) {
    }

    public static class PersonCursorPageableFactory extends DefaultCursorPageableFactory<Person, String> {
        @Override
        protected Cursor<String> createCursor(Person person) {
            return new Cursor<>(person.id(), person.name());
        }
    }

}