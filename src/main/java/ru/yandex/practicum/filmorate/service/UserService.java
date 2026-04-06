package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.exeption.NotFoundException;
import ru.yandex.practicum.filmorate.exeption.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;

@Service
@Slf4j
public class UserService {

  private final UserStorage userStorage;

  @Autowired
  public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
    this.userStorage = userStorage;
  }

  public Collection<User> findAll() {
    return userStorage.findAll();
  }

  public User findById(int id) throws NotFoundException {
    return getUserOrThrow(id);
  }

  public User create(@RequestBody User user) throws ValidationException {
    validateLogin(user);

    if (user.getName() == null || user.getName().isBlank()) {
      user.setName(user.getLogin());
    }

    user = userStorage.create(user);
    log.info("Добавлен пользователь: id={}, login={}", user.getId(), user.getLogin());
    return user;
  }

  public User update(@RequestBody User newUser) throws ValidationException, NotFoundException {
    User oldUser = getUserOrThrow(newUser.getId());
    if (newUser.getBirthday() != null) {
      if (newUser.getBirthday().isAfter(LocalDate.now())) {
        log.warn("Ошибка обновления: дата рождения {} не может быть в будущем.",
            newUser.getBirthday());
        throw new ValidationException("Дата рождения не может быть в будущем.");
      }
      oldUser.setBirthday(newUser.getBirthday());
      log.trace("обновлено поле: Birthday, новое значение: {}", newUser.getBirthday());
    }

    if (newUser.getLogin() != null) {
      validateLogin(newUser);
      oldUser.setLogin(newUser.getLogin());
      log.trace("обновлено поле: Login, новое значение: {}", newUser.getLogin());
    }

    if (newUser.getName() != null && !newUser.getName().isBlank()) {
      oldUser.setName(newUser.getName());
      log.debug("обновлено поле: Name, новое значение: {}", newUser.getName());
    }

    if (newUser.getEmail() != null) {
      if (newUser.getEmail().isBlank() || !newUser.getEmail().contains("@")) {
        log.error("Пользователь {} не прошел валидацию по полю Email: {}", newUser.getName(),
            newUser.getEmail());
        throw new ValidationException(
            "Электронная почта не может быть пустой и должна содержать символ @");
      }
      oldUser.setEmail(newUser.getEmail());
      log.debug("обновлено поле: Email, новое значение: {}", newUser.getEmail());
    }

    userStorage.update(oldUser);
    log.info("Пользователь id={} успешно обновлен", oldUser.getId());
    return oldUser;
  }

  public void addFriend(int id, int friendId) throws NotFoundException, ValidationException {
    checkNotEqualsId(id, friendId, "Попытка добавить самого себя в друзья: id=" + id);
    getUserOrThrow(id);
    getUserOrThrow(friendId);
    userStorage.addFriend(id, friendId);
    log.info("Пользователь {} добавил в друзья {}", id, friendId);
  }

  public Collection<User> findAllFriendsById(int id) throws NotFoundException {
    getUserOrThrow(id);

    return userStorage.getFriends(id);
  }

  public void deleteFriend(int id, int friendId) throws NotFoundException, ValidationException {
    checkNotEqualsId(id, friendId, "Попытка удаления самого себя из друзей: id=" + id);

    getUserOrThrow(id);
    getUserOrThrow(friendId);
    userStorage.removeFriend(id, friendId);
  }

  private void validateLogin(User user) throws ValidationException {
    if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
      log.error("Пользователь {} не прошел валидацию по полю: {}", user.getName(), user.getLogin());
      throw new ValidationException("Логин не может быть пустым и содержать пробелы");
    }
  }

  public User getUserOrThrow(int id) throws NotFoundException {

    return userStorage.findById(id).orElseThrow(() -> {
      log.warn("Не найден пользователь: id = {}", id);
      return new NotFoundException("Пользователь с Id " + id + " не найден");
    });
  }

  public Collection<User> getCommonFriends(int userId, int otherId) throws NotFoundException {
    getUserOrThrow(userId);
    getUserOrThrow(otherId);
    return userStorage.getCommonFriends(userId, otherId);
  }

  private void checkNotEqualsId(int userId, int friendId, String message)
      throws ValidationException {
    if (userId == friendId) {
      log.warn(message);
      throw new ValidationException(message);
    }
  }
}
