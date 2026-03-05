package ru.yandex.practicum.filmorate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exeption.NotFoundException;
import ru.yandex.practicum.filmorate.exeption.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Collection;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

public class FilmServiceTest {

  private static Validator validator;
  private static ValidatorFactory factory;
  private FilmService filmService;
  private UserService userService;

  @AfterAll
  static void tearDown() {
    if (factory != null) {
      factory.close();
    }
  }

  @BeforeEach
  void setUp() {
    FilmStorage filmStorage = new InMemoryFilmStorage();
    UserStorage userStorage = new InMemoryUserStorage();
    filmService = new FilmService(filmStorage, userStorage);
    userService = new UserService(userStorage);
    factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

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

    Film updatedData = new Film();
    updatedData.setId(film.getId());
    updatedData.setName("Inception Updated");
    updatedData.setDescription("New Description");

    Film result = filmService.update(updatedData);

    assertEquals(updatedData.getName(), result.getName());
    assertEquals(updatedData.getDescription(), result.getDescription());
    assertNotNull(result.getReleaseDate());
  }

  @Test
  void updateFilmSuccessfullyWithBlankName() throws ValidationException, NotFoundException {
    Film film = filmService.create(createValidFilm());

    Film updatedData = new Film();
    updatedData.setId(film.getId());
    updatedData.setName("   ");
    updatedData.setDescription("New Description");

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
    User user = new User();
    user.setEmail("test@test.ru");
    user.setLogin("test");
    user.setBirthday(LocalDate.now().minusYears(20));
    userService.create(user);

    filmService.addLike(film.getId(), user.getId());

    Collection<Film> films = filmService.getPopularFilms(1);
    assertTrue(films.contains(film));
    Film savedFilm = films.stream().findFirst().get();
    assertEquals(1, savedFilm.getLikes().size());
    assertTrue(savedFilm.getLikes().contains(user.getId()));
  }

  @Test
  void removeLikeSuccessfully() throws ValidationException, NotFoundException {
    Film film = filmService.create(createValidFilm());
    User user = new User();
    user.setEmail("test@test.ru");
    user.setLogin("test");
    user.setBirthday(LocalDate.now().minusYears(20));
    userService.create(user);

    filmService.addLike(film.getId(), user.getId());
    filmService.removeLike(film.getId(), user.getId());

    Collection<Film> films = filmService.getPopularFilms(1);
    assertTrue(films.contains(film));
    Film savedFilm = films.stream().findFirst().get();
    assertTrue(savedFilm.getLikes().isEmpty());
  }

  @Test
  void getPopularFilmsOrderByLikes() throws ValidationException {

    Film film1 = filmService.create(createValidFilm());
    Film film2 = filmService.create(createValidFilm());
    Film film3 = filmService.create(createValidFilm());

    User user = new User();
    user.setEmail("test@test.ru");
    user.setLogin("user");
    user.setBirthday(LocalDate.now().minusYears(20));
    userService.create(user);

    User user2 = new User();
    user2.setEmail("test2@test.ru");
    user2.setLogin("user2");
    user2.setBirthday(LocalDate.now().minusYears(20));
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
    User user = new User();
    user.setEmail("test@test.ru");
    user.setLogin("login");
    user.setBirthday(LocalDate.now().minusYears(10));
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

  private Film createValidFilm() {
    Film film = new Film();
    film.setName("Groundhog Day");
    film.setDescription(
        "Trapped in this time loop, he transforms from a selfish narcissist into a kinder person, ultimately finding love and breaking the curse.");
    film.setReleaseDate(LocalDate.of(1993, 7, 12));
    film.setDuration(Duration.ofMinutes(101));
    return film;
  }
}
