package ru.yandex.practicum.filmorate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exeption.NotFoundException;
import ru.yandex.practicum.filmorate.exeption.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

public class UserServiceTest {

  private static Validator validator;
  private static ValidatorFactory factory;
  private UserService userService = new UserService();

  @AfterAll
  static void tearDown() {
    if (factory != null) {
      factory.close();
    }
  }

  @BeforeEach
  void setUp() {
    userService = new UserService();
    factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  void createUserSuccessfully() throws ValidationException {
    User user = createValidUser();
    User savedUser = userService.create(user);

    assertNotNull(savedUser.getId());
    assertEquals(user, savedUser);
    assertEquals(1, userService.findAll().size());
  }

  @Test
  void createWithEmptyName() throws ValidationException {
    User user = createValidUser();
    user.setName("");

    User savedUser = userService.create(user);

    assertEquals("friend", savedUser.getName(), "Если имя пустое, должен использоваться логин");
  }

  @Test
  void createWithNullName() throws ValidationException {
    User user = createValidUser();
    user.setName(null);

    User savedUser = userService.create(user);

    assertEquals("friend", savedUser.getName(), "Если имя null, должен использоваться логин");
  }

  @Test
  void createWithTodayBirthday() throws ValidationException {
    User user = createValidUser();
    user.setBirthday(LocalDate.now());

    User savedUser = userService.create(user);

    assertNotNull(savedUser, "Дата рождения сегодня должна проходить валидация");
  }

  @Test
  void updateUserSuccessfully() throws ValidationException, NotFoundException {
    User user = userService.create(createValidUser());
    LocalDate birthday = LocalDate.of(2014, 11, 1);

    User updatedData = new User();
    updatedData.setId(user.getId());
    updatedData.setLogin("new_login");
    updatedData.setEmail("new@mail.ru");
    updatedData.setName("New Name");
    updatedData.setBirthday(birthday);

    User result = userService.update(updatedData);

    assertEquals("new_login", result.getLogin(), "Логин должен быть обновлен");
    assertEquals("new@mail.ru", result.getEmail(), "Email должен быть обновлен");
    assertEquals(updatedData.getName(), result.getName(), "Имя должно быть обновлено");
    assertEquals(birthday, result.getBirthday());
  }

  @Test
  void updateUserSuccessfullyWithBlankName() throws ValidationException, NotFoundException {
    User user = userService.create(createValidUser());
    LocalDate birthday = LocalDate.of(2014, 11, 1);

    User updatedData = new User();
    updatedData.setId(user.getId());
    updatedData.setLogin("new_login");
    updatedData.setEmail("new@test.ru");
    updatedData.setName("");
    updatedData.setBirthday(birthday);

    User result = userService.update(updatedData);

    assertEquals("new_login", result.getLogin());
    assertEquals("new@test.ru", result.getEmail());
    assertEquals(user.getName(), result.getName(), "Имя не должно затираться при пустом новом");
    assertEquals(birthday, result.getBirthday());
  }

  @Test
  void getAllUsers() throws ValidationException {
    userService.create(createValidUser());
    User user2 = createValidUser();
    user2.setLogin("second");
    userService.create(user2);

    Collection<User> users = userService.findAll();

    assertEquals(2, users.size());
  }

  @Test
  void createFailureWithUserWithBlankLogin() {
    User user = createValidUser();
    user.setLogin("  ");

    assertThrows(ValidationException.class, () -> userService.create(user));
  }

  @Test
  void updatedFailureWithUserWithWrongEmail() throws ValidationException {
    User user = userService.create(createValidUser());

    User updatedData = new User();
    updatedData.setId(user.getId());
    updatedData.setEmail("wrong mail");
    assertThrows(ValidationException.class, () -> userService.update(updatedData));
  }

  @Test
  void createFailureWithUserWithWrongEmail() {
    User user = createValidUser();
    user.setEmail("test");
    Set<ConstraintViolation<User>> validated = validator.validate(user);
    boolean hasEmailViolation = validated.stream()
        .anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    assertTrue(hasEmailViolation, "Поле email не должно пройти валидацию по паттерну");
  }

  @Test
  void createFailureWithBirthdayInFuture() {
    User user = createValidUser();
    user.setBirthday(LocalDate.now().plusDays(1));
    Set<ConstraintViolation<User>> validated = validator.validate(user);
    boolean hasEmailViolation = validated.stream()
        .anyMatch(v -> v.getPropertyPath().toString().equals("birthday"));
    assertTrue(hasEmailViolation, "Поле Birthday должно содержать прошедшую дату");
  }

  @Test
  void createFailureWithUserWithLoginContainsSpaces() {
    User user = createValidUser();
    user.setLogin("bad login");

    ValidationException exception = assertThrows(ValidationException.class, () -> {
      userService.create(user);
    });

    assertEquals("Логин не может быть пустым и содержать пробелы", exception.getMessage());
  }

  @Test
  void updateFailureWithBirthdayInFuture() throws ValidationException {
    User user = userService.create(createValidUser());

    User updatedData = new User();
    updatedData.setId(user.getId());
    updatedData.setBirthday(LocalDate.now().plusDays(1));
    assertThrows(ValidationException.class, () -> userService.update(updatedData));
  }

  @Test
  void updateFailureWithNonExistentUser() {
    User user = createValidUser();
    user.setId(999);

    assertThrows(NotFoundException.class, () -> userService.update(user));
  }

  private User createValidUser() {
    User user = new User();
    user.setEmail("test@test.ru");
    user.setLogin("friend");
    user.setName("Common Name");
    user.setBirthday(LocalDate.of(1985, 9, 20));
    return user;
  }
}
