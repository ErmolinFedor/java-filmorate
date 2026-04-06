package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exeption.NotFoundException;
import ru.yandex.practicum.filmorate.exeption.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DbDirectorServiceTest {

  private final DirectorService directorService;


  private final JdbcTemplate jdbcTemplate;

  @BeforeEach
  public void setUp() {
    jdbcTemplate.update("DELETE FROM directors");
    jdbcTemplate.update("ALTER TABLE directors ALTER COLUMN id RESTART WITH 1");
  }

  @Test
  void createDirectorSuccessfully() throws ValidationException {
    Director director = createValidDirector();
    Director savedDirector = directorService.create(director);

    assertNotNull(savedDirector.getId(), "ID не должен быть null после создания");
    assertEquals(director, savedDirector);
    assertEquals(1, directorService.findAll().size());
  }

  @Test
  void createAndIncrementIdWithMultipleDirectorAdded() throws ValidationException {
    Director director1 = createValidDirector();
    Director director2 = createValidDirector();

    director2.setName("The Batman");

    Director saved1 = directorService.create(director1);
    Director saved2 = directorService.create(director2);

    assertEquals(1, saved1.getId());
    assertEquals(2, saved2.getId());
  }

  @Test
  void updateDirectorSuccessfully() throws ValidationException, NotFoundException {
    Director director = directorService.create(createValidDirector());
    Director updatedData = Director.builder().id(director.getId()).name("Inception Updated").build();
    Director result = directorService.update(updatedData);
    assertEquals(updatedData.getName(), result.getName());
  }

  @Test
  void updateFailureWithNonExistentDirector() {
    Director director = createValidDirector();
    director.setId(999);

    NotFoundException exception = assertThrows(NotFoundException.class, () -> {
      directorService.update(director);
    });

    assertTrue(exception.getMessage().contains("не найден"));
  }

  @Test
  void findAllDirectorsSuccessfully() throws ValidationException {
    directorService.create(Director.builder().name("aa").build());
    directorService.create(Director.builder().name("aa1").build());
    Collection<Director> directors = directorService.findAll();

    assertEquals(2, directors.size(), "Количество режиссёров должно быть 2");
  }

  @Test
  void findDirectorByIdSuccessfully() throws ValidationException {
    directorService.create(Director.builder().name("aa").build());
    directorService.create(Director.builder().name("aa1").build());
    Director director = directorService.getDirectorById(1);

    assertNotNull(director);
    assertEquals("aa", director.getName(), "Режиссёр с id=1 должен быть 'aa'");

    Director drama = directorService.getDirectorById(2);
    assertEquals("aa1", drama.getName(), "Режиссёр с id=2 должен быть 'aa1'");
  }

  @Test
  void findDirectorByIdThrowsNotFoundException() {
    int nonExistentId = 999;

    NotFoundException exception = assertThrows(NotFoundException.class,
        () -> directorService.getDirectorById(nonExistentId));

    assertTrue(exception.getMessage().contains("не найден"),
        "Сообщение должно содержать текст о ненахождении режиссёра");
  }

  @Test
  void deleteDirector() throws ValidationException {
    Director director1 = createValidDirector();
    Director director2 = createValidDirector();
    director2.setName("The Batman");

    Director saved1 = directorService.create(director1);
    Director saved2 = directorService.create(director2);

    directorService.remove(saved1.getId());

    assertEquals(1, directorService.findAll().size());
    assertEquals(2, saved2.getId());
  }

  @Test
  void deleteDirectorByIdThrowsNotFoundException() {
    int nonExistentId = 999;

    NotFoundException exception = assertThrows(NotFoundException.class,
        () -> directorService.remove(nonExistentId));

    assertTrue(exception.getMessage().contains("не найден"),
        "Сообщение должно содержать текст о ненахождении режиссёра");
  }

  protected Director createValidDirector() {
    return Director.builder().name("Groundhog Day").build();
  }
}
