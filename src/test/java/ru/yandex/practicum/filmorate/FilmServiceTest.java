package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exeption.NotFoundException;
import ru.yandex.practicum.filmorate.exeption.ValidationException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.service.DirectorService;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public abstract class FilmServiceTest<T extends FilmStorage> extends BaseServiceTest {

  protected FilmService filmService;
  protected UserService userService;
  protected DirectorService directorService;

  @BeforeEach
  protected abstract void setUp();

  @Test
  void createFilmSuccessfully() throws ValidationException {
    Film film = createValidFilm();
    Film savedFilm = filmService.create(film);

    assertNotNull(savedFilm.getId(), "ID не должен быть null после создания");
    assertEquals(film, savedFilm);
    assertEquals(1, filmService.findAll().size());
  }

  @Test
  void createFilmSuccessfullyWithOnlyRequired() throws ValidationException {
    Film film = createValidFilm();
    film.setDescription(null);
    Film savedFilm = filmService.create(film);

    assertNotNull(savedFilm.getId(), "ID не должен быть null после создания");
    assertEquals(film, savedFilm);
    assertEquals(1, filmService.findAll().size());
  }

  @Test
  void createAndIncrementIdWithMultipleFilmsAdded() throws ValidationException {
    Film film1 = createValidFilm();
    Film film2 = createValidFilm();
    film2.setName("The Batman");

    Film saved1 = filmService.create(film1);
    Film saved2 = filmService.create(film2);

    assertEquals(1, saved1.getId());
    assertEquals(2, saved2.getId());
  }

  @Test
  void updateFilmSuccessfully() throws ValidationException, NotFoundException {
    Film film = filmService.create(createValidFilm());

    Film updatedData = Film.builder().id(film.getId()).name("Inception Updated")
        .description("New Description").build();

    Film result = filmService.update(updatedData);

    assertEquals(updatedData.getName(), result.getName());
    assertEquals(updatedData.getDescription(), result.getDescription());
    assertNotNull(result.getReleaseDate());
  }

  @Test
  void updateFilmSuccessfullyWithBlankName() throws ValidationException, NotFoundException {
    Film film = filmService.create(createValidFilm());

    Film updatedData = Film.builder().id(film.getId()).name("   ").description("New Description")
        .build();

    Film result = filmService.update(updatedData);

    assertEquals(film.getName(), result.getName());
    assertEquals(updatedData.getDescription(), result.getDescription());
    assertNotNull(result.getReleaseDate());
  }

  @Test
  void getAllFilms() throws ValidationException {
    filmService.create(createValidFilm());
    filmService.create(createValidFilm());

    Collection<Film> films = filmService.findAll();

    assertEquals(2, films.size());
  }

  @Test
  void addLikeSuccessfully() throws ValidationException {
    Film film = filmService.create(createValidFilm());
    User user = User.builder()
        .email("test@test.ru")
        .login("test")
        .birthday(LocalDate.now().minusYears(20))
        .build();
    userService.create(user);

    filmService.addLike(film.getId(), user.getId());

    Collection<Film> popularFilms = filmService.getPopularFilms(10);

    Optional<Film> savedFilmOpt = popularFilms.stream()
        .filter(f -> f.getId().equals(film.getId()))
        .findFirst();

    assertTrue(savedFilmOpt.isPresent(), "Фильм должен быть найден в списке популярных");

    Film savedFilm = savedFilmOpt.get();

    assertEquals(1, savedFilm.getLikes().size(), "Количество лайков должно быть 1");
    assertTrue(savedFilm.getLikes().contains(user.getId()), "Лайк должен принадлежать нашему пользователю");

  }

  @Test
  void removeLikeSuccessfully() throws ValidationException, NotFoundException {
    Film film = filmService.create(createValidFilm());
    User user = User.builder().email("test@test.ru").login("test")
        .birthday(LocalDate.now().minusYears(20)).build();
    userService.create(user);

    filmService.addLike(film.getId(), user.getId());
    filmService.removeLike(film.getId(), user.getId());

    Collection<Film> films = filmService.getPopularFilms(1);
    boolean isFound = films.stream().anyMatch(f -> f.getId().equals(film.getId()));
    assertTrue(isFound, "Фильм должен присутствовать в списке популярных");

    Film savedFilm = films.stream().filter(f -> f.getId().equals(film.getId())).findFirst().get();
    assertTrue(savedFilm.getLikes().isEmpty(), "Список лайков должен быть пуст");
  }

  @Test
  void getPopularFilmsOrderByLikes() throws ValidationException {

    Film film1 = filmService.create(createValidFilm());
    Film film2 = filmService.create(createValidFilm());
    Film film3 = filmService.create(createValidFilm());

    User user = User.builder().email("test@test.ru").login("user")
        .birthday(LocalDate.now().minusYears(20)).build();
    userService.create(user);

    User user2 = User.builder().email("test2@test.ru").login("user2")
        .birthday(LocalDate.now().minusYears(20)).build();
    userService.create(user2);

    filmService.addLike(film2.getId(), user.getId());
    filmService.addLike(film2.getId(), user2.getId());

    filmService.addLike(film1.getId(), user.getId());

    Collection<Film> popular = filmService.getPopularFilms(10);
    Object[] popularArr = popular.toArray();

    assertEquals(3, popular.size());
    assertEquals(film2.getId(), ((Film) popularArr[0]).getId(),
        "Первым должен быть фильм с 2 лайками");
    assertEquals(film1.getId(), ((Film) popularArr[1]).getId(),
        "Вторым должен быть фильм с 1 лайком");
  }

  @Test
  void getPopularFilmsWithLimit() throws ValidationException {
    filmService.create(createValidFilm());
    filmService.create(createValidFilm());
    filmService.create(createValidFilm());

    Collection<Film> popular = filmService.getPopularFilms(2);

    assertEquals(2, popular.size(), "Должно вернуться только 2 фильма согласно лимиту");
  }

  @Test
  void addLikeByDoesNotExistUser() throws ValidationException {
    Film film = filmService.create(createValidFilm());
    int nonExistentUserId = 999;

    NotFoundException exception = assertThrows(NotFoundException.class, () -> {
      filmService.addLike(film.getId(), nonExistentUserId);
    });

    assertTrue(exception.getMessage().contains("не найден"),
        "Сообщение должно содержать текст об отсутствии");
  }

  @Test
  void addLikeToDoesNotExistFilm() throws ValidationException {
    User user = User.builder().email("test@test.ru").login("login")
        .birthday(LocalDate.now().minusYears(10)).build();
    userService.create(user);

    int nonExistentFilmId = 999;

    assertThrows(NotFoundException.class, () -> {
      filmService.addLike(nonExistentFilmId, user.getId());
    });
  }

  @Test
  void removeLikeFromDoesNotExistFilm() {
    assertThrows(NotFoundException.class, () -> {
      filmService.removeLike(999, 1);
    });
  }

  @Test
  void getaFailurePopularFilmsWithNegativeLimit() throws ValidationException {
    filmService.create(createValidFilm());

    assertThrows(ValidationException.class, () -> filmService.getPopularFilms(-1));
  }

  @Test
  void createFailureWithBlankDescription() {
    Film film = createValidFilm();
    film.setDescription("   ");

    assertThrows(ValidationException.class, () -> filmService.create(film));
  }

  @Test
  void updateFailureWithTooLongDescription() {
    Film film = createValidFilm();

    film.setDescription("A".repeat(201));

    ValidationException exception = assertThrows(ValidationException.class, () -> {
      filmService.create(film);
    });

    assertEquals("Максимальная длина описания — 200 символов", exception.getMessage());
  }

  @Test
  void createFailureWithNullDuration() {
    Film film = createValidFilm();
    film.setDuration(null);

    Set<ConstraintViolation<Film>> validated = validator.validate(film);

    boolean hasEmailViolation = validated.stream()
        .anyMatch(v -> v.getPropertyPath().toString().equals("duration"));
    assertTrue(hasEmailViolation, "Поле duration не может быть null");
  }

  @Test
  void createFailureWithNegativeDuration() {
    Film film = createValidFilm();
    film.setDuration(Duration.ofMinutes(-1));

    Set<ConstraintViolation<Film>> validated = validator.validate(film);

    boolean hasEmailViolation = validated.stream()
        .anyMatch(v -> v.getPropertyPath().toString().equals("duration"));
    assertTrue(hasEmailViolation, "Поле duration не может быть отрицательным");
  }

  @Test
  void createFailureWithZeroDuration() {
    Film film = createValidFilm();
    film.setDuration(Duration.ZERO);

    Set<ConstraintViolation<Film>> validated = validator.validate(film);

    boolean hasEmailViolation = validated.stream()
        .anyMatch(v -> v.getPropertyPath().toString().equals("duration"));
    assertTrue(hasEmailViolation, "Поле duration не может быть 0");
  }

  @Test
  void createFailureWithWrongReleaseDateNull() {
    Film film = createValidFilm();
    film.setReleaseDate(null);
    Set<ConstraintViolation<Film>> validated = validator.validate(film);

    boolean hasEmailViolation = validated.stream()
        .anyMatch(v -> v.getPropertyPath().toString().equals("releaseDate"));
    assertTrue(hasEmailViolation, "Поле releaseDate не может быть null");
  }

  @Test
  void createFailureWithWrongReleaseDatePast() {
    Film film = createValidFilm();
    film.setReleaseDate(LocalDate.of(1895, 12, 27));
    Set<ConstraintViolation<Film>> validated = validator.validate(film);

    boolean hasEmailViolation = validated.stream()
        .anyMatch(v -> v.getPropertyPath().toString().equals("releaseDate"));
    assertTrue(hasEmailViolation, "Поле releaseDate не может быть раньше 28 декабря 1895 года");
  }

  @Test
  void createFailureWithWrongReleaseDateFuture() {
    Film film = createValidFilm();
    film.setReleaseDate(LocalDate.now().plusDays(1));
    Set<ConstraintViolation<Film>> validated = validator.validate(film);

    boolean hasEmailViolation = validated.stream()
        .anyMatch(v -> v.getPropertyPath().toString().equals("releaseDate"));
    assertTrue(hasEmailViolation, "Поле releaseDate не может быть в будущем");
  }

  @Test
  void updateFailureWithNonExistentFilm() {
    Film film = createValidFilm();
    film.setId(999);

    NotFoundException exception = assertThrows(NotFoundException.class, () -> {
      filmService.update(film);
    });

    assertTrue(exception.getMessage().contains("не найден"));
  }

  @Test
  void createFilmWithDirectorSuccessfully() throws ValidationException {
    Film film = createValidFilm();
    Director director = Director.builder().name("Groundhog Day").build();

    directorService.create(director);
    film.getDirectors().add(director);

    Film filmSaved = filmService.create(film);

    assertEquals(1, filmSaved.getDirectors().size());
  }

  @Test
  void getFilmsByDirectorByYearsSuccessfully() throws ValidationException {
    Film film1 = createValidFilm();
    Film film2 = createValidFilm();
    film2.setReleaseDate(LocalDate.of(1994, 7, 12));
    Director director = Director.builder().name("Groundhog Day").build();

    Director directorSaved = directorService.create(director);
    film1.getDirectors().add(director);
    film2.getDirectors().add(director);

    Film filmSaved1 = filmService.create(film1);
    Film filmSaved2 = filmService.create(film2);

    Collection<Film> filmsByDirector = filmService.getFilmsByDirector(directorSaved.getId(), SortBy.year);

    assertEquals(2, filmsByDirector.size());
    assertIterableEquals(List.of(filmSaved1, filmSaved2), filmsByDirector);
  }

  @Test
  void searchFilmByDirectorSuccessfully() throws ValidationException {
    Film film1 = createValidFilm();
    Film film2 = createValidFilm();
    Director director1 = Director.builder().name("Groundhog Day").build();
    Director director2 = Director.builder().name("Groundhog").build();

    directorService.create(director1);
    directorService.create(director2);
    film1.getDirectors().add(director1);
    film2.getDirectors().add(director2);

    Film filmSaved1 = filmService.create(film1);
    Film filmSaved2 = filmService.create(film2);

    Collection<Film> filmsByDirector = filmService.searchByDirectorAndName("day", List.of(SearchType.director));

    assertEquals(1, filmsByDirector.size());
    assertIterableEquals(List.of(filmSaved1), filmsByDirector);
  }

  @Test
  void searchFilmsByDirectorSuccessfully() throws ValidationException {
    Film film1 = createValidFilm();
    Film film2 = createValidFilm();
    Director director1 = Director.builder().name("Groundhog Day").build();
    Director director2 = Director.builder().name("Groundhog").build();

    directorService.create(director1);
    directorService.create(director2);
    film1.getDirectors().add(director1);
    film2.getDirectors().add(director2);

    Film filmSaved1 = filmService.create(film1);
    Film filmSaved2 = filmService.create(film2);

    Collection<Film> filmsByDirector = filmService.searchByDirectorAndName("Groundhog", List.of(SearchType.director));

    assertEquals(2, filmsByDirector.size());
    assertIterableEquals(List.of(filmSaved1, filmSaved2), filmsByDirector);
  }

  @Test
  void searchFilmsByTitleSuccessfully() throws ValidationException {
    Film film1 = createValidFilm();
    Film film2 = createValidFilm();
    film2.setName("Groundhog");
    Director director1 = Director.builder().name("Groundhog Day").build();
    Director director2 = Director.builder().name("Groundhog").build();

    directorService.create(director1);
    directorService.create(director2);
    film1.getDirectors().add(director1);
    film2.getDirectors().add(director2);

    Film filmSaved1 = filmService.create(film1);
    Film filmSaved2 = filmService.create(film2);

    Collection<Film> filmsByDirector = filmService.searchByDirectorAndName("Day", List.of(SearchType.title));

    assertEquals(1, filmsByDirector.size());
    assertIterableEquals(List.of(filmSaved1), filmsByDirector);
  }

  @Test
  void searchFilmsByTitleAndDirectorSuccessfully() throws ValidationException {
    Film film1 = createValidFilm();
    Film film2 = createValidFilm();
    film2.setName("Groundhog");
    Director director1 = Director.builder().name("Groundhog").build();
    Director director2 = Director.builder().name("Groundhog Day").build();

    directorService.create(director1);
    directorService.create(director2);
    film1.getDirectors().add(director1);
    film2.getDirectors().add(director2);

    Film filmSaved1 = filmService.create(film1);
    Film filmSaved2 = filmService.create(film2);

    Collection<Film> filmsByDirector = filmService.searchByDirectorAndName("Day", List.of(SearchType.title, SearchType.director));

    assertEquals(2, filmsByDirector.size());
    assertIterableEquals(List.of(filmSaved1, film2), filmsByDirector);
  }

  @Test
  void getFilmsByDirectorByLikesSuccessfully() throws ValidationException {
    Film film1 = createValidFilm();
    Film film2 = createValidFilm();
    Director director = Director.builder().name("Groundhog Day").build();

    Director directorSaved = directorService.create(director);
    film1.getDirectors().add(director);
    film2.getDirectors().add(director);

    User user = User.builder().email("test@test.ru").login("user")
        .birthday(LocalDate.now().minusYears(20)).build();
    userService.create(user);

    User user2 = User.builder().email("test2@test.ru").login("user2")
        .birthday(LocalDate.now().minusYears(20)).build();
    userService.create(user2);

    Film filmSaved1 = filmService.create(film1);
    Film filmSaved2 = filmService.create(film2);

    filmService.addLike(filmSaved2.getId(), user.getId());
    filmService.addLike(filmSaved2.getId(), user2.getId());
    filmSaved2.getLikes().add(user.getId());
    filmSaved2.getLikes().add(user2.getId());

    filmSaved1.getLikes().add(user.getId());
    filmService.addLike(filmSaved1.getId(), user.getId());

    Collection<Film> filmsByDirector = filmService.getFilmsByDirector(directorSaved.getId(), SortBy.likes);

    assertEquals(2, filmsByDirector.size());
    assertIterableEquals(List.of(filmSaved2, filmSaved1), filmsByDirector);
  }

  protected Film createValidFilm() {
    return Film.builder().name("Groundhog Day").description(
            "Trapped in this time loop, he transforms from a selfish narcissist into a kinder person, ultimately finding love and breaking the curse.")
        .releaseDate(LocalDate.of(1993, 7, 12)).duration(Duration.ofMinutes(101))
        .mpa(new Mpa(1, "G")).build();
  }
}
