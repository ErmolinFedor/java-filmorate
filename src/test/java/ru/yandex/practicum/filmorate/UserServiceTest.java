package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exeption.NotFoundException;
import ru.yandex.practicum.filmorate.exeption.ValidationException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public abstract class UserServiceTest<T extends UserStorage> extends BaseServiceTest {

  protected UserStorage userStorage;
  protected UserService userService;

  @BeforeEach
  protected abstract void setUp();

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

    assertEquals("user", savedUser.getName(), "Если имя пустое, должен использоваться логин");
  }

  @Test
  void createWithNullName() throws ValidationException {
    User user = createValidUser();
    user.setName(null);

    User savedUser = userService.create(user);

    assertEquals("user", savedUser.getName(), "Если имя null, должен использоваться логин");
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

    User updatedData = User.builder().id(user.getId()).login("new_login").email("new@mail.ru")
        .name("New Name").birthday(birthday).build();

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

    User updatedData = User.builder().id(user.getId()).login("new_login").email("new@test.ru")
        .name("").birthday(birthday).build();

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
  void getUsersById() throws ValidationException, NotFoundException {
    User user = userService.create(createValidUser());
    User actualUser = userService.findById(user.getId());

    assertNotNull(actualUser);
    assertEquals(user, actualUser);
  }

  @Test
  void addFriendSuccessfully() throws ValidationException, NotFoundException {
    User user = userService.create(createValidUser());
    User friend = createValidUser();
    friend.setLogin("friend");
    friend = userService.create(friend);

    userService.addFriend(user.getId(), friend.getId());

    User actualUser = userService.findById(user.getId());
    User actualFriend = userService.findById(friend.getId());

    assertNotNull(actualUser);
    assertNotNull(actualFriend);

    assertTrue(actualUser.getFriends().contains(friend.getId()));
    assertTrue(actualFriend.getFriends().contains(user.getId()));
  }

  @Test
  void getEmptyFriendSuccessfully() throws ValidationException, NotFoundException {
    User user = userService.create(createValidUser());

    Collection<User> friends = userService.findAllFriendsById(user.getId());
    assertTrue(friends.isEmpty());
  }

  @Test
  void getFriendSuccessfully() throws ValidationException, NotFoundException {
    User user = userService.create(createValidUser());
    User friend = createValidUser();
    friend.setLogin("friend");
    friend = userService.create(friend);

    userService.addFriend(user.getId(), friend.getId());

    Collection<User> friends = userService.findAllFriendsById(user.getId());
    assertFalse(friends.isEmpty());
    assertEquals(friend, friends.stream().findFirst().get());
  }

  @Test
  void getCommonFriendSuccessfully() throws ValidationException, NotFoundException {
    User user = userService.create(createValidUser());
    User friend1 = createValidUser();
    friend1.setLogin("friend1");
    friend1 = userService.create(friend1);

    userService.addFriend(user.getId(), friend1.getId());

    User friend2 = createValidUser();
    friend2.setLogin("friend2");
    friend2 = userService.create(friend2);

    userService.addFriend(friend1.getId(), friend2.getId());

    Collection<User> commonFriends = userService.getCommonFriends(user.getId(), friend2.getId());
    assertFalse(commonFriends.isEmpty());
    assertEquals(friend1, commonFriends.stream().findFirst().get());
  }

  @Test
  void getEmptyCommonWhenInFriendsSuccessfully() throws ValidationException, NotFoundException {
    User user = userService.create(createValidUser());
    User friend1 = createValidUser();
    friend1.setLogin("friend1");
    friend1 = userService.create(friend1);

    userService.addFriend(user.getId(), friend1.getId());

    Collection<User> commonFriends = userService.getCommonFriends(user.getId(), friend1.getId());
    assertTrue(commonFriends.isEmpty());
  }

  @Test
  void getCommonEmptyFriendsSuccessfully() throws ValidationException, NotFoundException {
    User user = userService.create(createValidUser());
    User friend1 = createValidUser();
    friend1.setLogin("friend1");
    friend1 = userService.create(friend1);

    userService.addFriend(user.getId(), friend1.getId());

    User friend2 = createValidUser();
    friend2.setLogin("friend2");
    friend2 = userService.create(friend2);

    Collection<User> commonFriends = userService.getCommonFriends(user.getId(), friend2.getId());
    assertTrue(commonFriends.isEmpty());
  }

  @Test
  void deleteFriendSuccessfully() throws ValidationException, NotFoundException {
    User user = userService.create(createValidUser());
    User friend = createValidUser();
    friend.setLogin("friend");
    friend = userService.create(friend);

    userService.addFriend(user.getId(), friend.getId());

    Collection<User> friends = userService.findAllFriendsById(user.getId());
    assertFalse(friends.isEmpty());
    assertEquals(friend, friends.stream().findFirst().get());

    userService.deleteFriend(user.getId(), friend.getId());
    Collection<User> friendsAfterDel = userService.findAllFriendsById(user.getId());
    assertTrue(friendsAfterDel.isEmpty());
  }

  @Test
  void deleteFriendFailureWhenDeleteSelfAsFriend() throws ValidationException {
    User user = userService.create(createValidUser());

    assertThrows(ValidationException.class,
        () -> userService.deleteFriend(user.getId(), user.getId()));
  }

  @Test
  void deleteFriendFailureWhenUserDoesNotExist() throws ValidationException {
    User user = userService.create(createValidUser());

    assertThrows(NotFoundException.class, () -> userService.deleteFriend(user.getId(), 999));
  }

  @Test
  void addFriendFailureWhenAddSelfAsFriend() throws ValidationException, NotFoundException {
    User user = userService.create(createValidUser());

    assertThrows(ValidationException.class,
        () -> userService.addFriend(user.getId(), user.getId()));
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

    User updatedData = User.builder().id(user.getId()).email("wrong mail").build();
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

    User updatedData = User.builder().id(user.getId()).birthday(LocalDate.now().plusDays(1))
        .build();
    assertThrows(ValidationException.class, () -> userService.update(updatedData));
  }

  @Test
  void updateFailureWithNonExistentUser() {
    User user = createValidUser();
    user.setId(999);

    assertThrows(NotFoundException.class, () -> userService.update(user));
  }

  private User createValidUser() {
    return User.builder().email("test@test.ru").login("user").name("Common Name")
        .birthday(LocalDate.of(1985, 9, 20)).build();
  }

  @Test
  void getFeedEmptyInitially() throws ValidationException {
    User user = userService.create(createValidUser());

    Collection<Event> feed = userService.getFeed(user.getId());

    assertTrue(feed.isEmpty(), "Лента нового пользователя должна быть пустой");
  }

  @Test
  void getFeedAfterAddingAndDeletingFriend() throws ValidationException, NotFoundException {
    User user = userService.create(createValidUser());
    User friend = createValidUser();
    friend.setLogin("friend");
    friend = userService.create(friend);

    userService.addFriend(user.getId(), friend.getId());
    userService.deleteFriend(user.getId(), friend.getId());

    List<Event> feed = new ArrayList<>(userService.getFeed(user.getId()));

    assertEquals(2, feed.size(), "В ленте должно быть 2 события");

    assertEquals("FRIEND", feed.getFirst().getEventType().name());
    assertEquals("ADD", feed.get(0).getOperation().toString());
    assertEquals(friend.getId(), feed.get(0).getEntityId());

    assertEquals("FRIEND", feed.get(1).getEventType().name());
    assertEquals("REMOVE", feed.get(1).getOperation().toString());
    assertEquals(friend.getId(), feed.get(1).getEntityId());
  }

  @Test
  void getFeedThrowsNotFoundForInvalidUser() {
    assertThrows(NotFoundException.class, () -> userService.getFeed(999),
        "Должно выбрасываться исключение для несуществующего пользователя");
  }

}
