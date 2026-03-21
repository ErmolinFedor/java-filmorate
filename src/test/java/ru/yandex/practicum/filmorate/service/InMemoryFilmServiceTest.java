package ru.yandex.practicum.filmorate.service;

import jakarta.validation.Validation;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import ru.yandex.practicum.filmorate.FilmServiceTest;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.genres.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

public class InMemoryFilmServiceTest extends FilmServiceTest<InMemoryFilmStorage> {

  @Override
  @BeforeEach
  public void setUp() {
    FilmStorage filmStorage = new InMemoryFilmStorage();
    UserStorage userStorage = new InMemoryUserStorage();
    MpaStorage mpaStorage = new MpaStorage() {

      @Override
      public Collection<Mpa> findAll() {
        return List.of();
      }

      @Override
      public Optional<Mpa> findById(int id) {
        return Optional.of(new Mpa(id, "G"));
      }
    };

    GenreStorage genreStorage = new GenreStorage() {

      @Override
      public Collection<Genre> findAll() {
        return List.of();
      }

      @Override
      public Optional<Genre> findById(int id) {
        return Optional.of(new Genre(id, "Action"));
      }
    };

    this.filmService = new FilmService(filmStorage, userStorage, mpaStorage, genreStorage);

    userService = new UserService(userStorage);
    factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }
}
