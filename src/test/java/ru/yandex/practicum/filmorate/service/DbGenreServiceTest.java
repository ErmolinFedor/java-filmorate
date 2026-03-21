package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exeption.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DbGenreServiceTest {

  private final GenreService genreService;

  @Test
  void findAllGenresSuccessfully() {
    Collection<Genre> genres = genreService.findAll();

    assertEquals(6, genres.size(), "Количество жанров должно быть 6");
  }

  @Test
  void findGenreByIdSuccessfully() {
    Genre genre = genreService.findById(1);

    assertNotNull(genre);
    assertEquals("Комедия", genre.getName(), "Жанр с id=1 должен быть 'Комедия'");

    Genre drama = genreService.findById(2);
    assertEquals("Драма", drama.getName(), "Жанр с id=2 должен быть 'Драма'");
  }

  @Test
  void findGenreByIdThrowsNotFoundException() {
    int nonExistentId = 999;

    NotFoundException exception = assertThrows(NotFoundException.class,
        () -> genreService.findById(nonExistentId));

    assertTrue(exception.getMessage().contains("не найден"),
        "Сообщение должно содержать текст о ненахождении жанра");
  }
}
