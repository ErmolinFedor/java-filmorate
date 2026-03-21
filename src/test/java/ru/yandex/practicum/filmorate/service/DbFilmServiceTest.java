package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.FilmServiceTest;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class DbFilmServiceTest extends FilmServiceTest<FilmDbStorage> {

  private final FilmService springFilmService;
  private final UserDbStorage userDbStorage;
  private final JdbcTemplate jdbcTemplate;

  @Override
  @BeforeEach
  public void setUp() {
    jdbcTemplate.update("DELETE FROM likes");
    jdbcTemplate.update("DELETE FROM film_genres");
    jdbcTemplate.update("DELETE FROM films");
    jdbcTemplate.update("ALTER TABLE films ALTER COLUMN id RESTART WITH 1");
    jdbcTemplate.update("DELETE FROM friends");
    jdbcTemplate.update("DELETE FROM users");
    jdbcTemplate.update("ALTER TABLE users ALTER COLUMN id RESTART WITH 1");

    this.filmService = springFilmService;
    this.userService = new UserService(userDbStorage);
  }
}
