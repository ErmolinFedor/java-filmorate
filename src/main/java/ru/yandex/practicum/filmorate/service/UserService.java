package ru.yandex.practicum.filmorate.service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.exeption.NotFoundException;
import ru.yandex.practicum.filmorate.exeption.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

@Service
@Slf4j
public class UserService {

  private final UserStorage userStorage;

  @Autowired
  public UserService(@Qualifier("inMemoryUserStorage") UserStorage userStorage) {
    this.userStorage = userStorage;
  }

  public Collection<User> findAll() {
    return userStorage.findAll();
  }

  public Optional<User> findById(int id) throws NotFoundException {
    Optional<User> userOptional = userStorage.findById(id);
    if (userOptional.isEmpty()) {
      log.warn("Не найден пользователь: id={}", id);
      throw new NotFoundException("Пользователь с Id " + id + " не найден");
    }
    return userOptional;
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
    User oldUser = getOrThrow(newUser.getId());
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
    if (id == friendId) {
      log.warn("Попытка добавить самого себя в друзья: id={}", id);
      throw new ValidationException("Пользователь не может добавить самого себя в друзья");
    }
    User user = getOrThrow(id);
    User friend = getOrThrow(friendId);

    user.getFriends().add(friendId);
    friend.getFriends().add(user.getId());

    userStorage.update(user);
    userStorage.update(friend);
  }

  public Collection<User> findAllFriendsById(int id) throws NotFoundException {
    User user = getOrThrow(id);

    return user.getFriends().stream()
        .map(userStorage::findById)
        .flatMap(Optional::stream)
        .collect(Collectors.toList());
  }

  public void deleteFriend(int id, int friendId) throws NotFoundException, ValidationException {
    if (id == friendId) {
      log.warn("Попытка удаления самого себя из друзей: id={}", id);
      throw new ValidationException("Попытка удаления самого себя из друзей: id=" + id);
    }
    User user = getOrThrow(id);
    User friend = getOrThrow(friendId);

    boolean removedFromUser = user.getFriends().remove(friendId);
    boolean removedFromFriend = friend.getFriends().remove(id);

    if (removedFromUser || removedFromFriend) {
      userStorage.update(user);
      userStorage.update(friend);
      log.info("Пользователи {} и {} успешно удалены из друзей друг у друга", id, friendId);
    } else {
      log.info("Пользователи {} и {} не являлись друзьями, удаление не требуется", id, friendId);
    }
  }

  private void validateLogin(User user) throws ValidationException {
    if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
      log.error("Пользователь {} не прошел валидацию по полю: {}", user.getName(), user.getLogin());
      throw new ValidationException("Логин не может быть пустым и содержать пробелы");
    }
  }

  private User getOrThrow(int id) throws NotFoundException {

    return userStorage.findById(id)
        .orElseThrow(() -> {
          log.warn("Не найден пользователь: id = {}", id);
          return new NotFoundException("Пользователь с Id " + id + " не найден");
        });
  }

  public Collection<User> getCommonFriends(int userId, int otherId) throws NotFoundException {
    User user = getOrThrow(userId);
    User friend = getOrThrow(otherId);
    Set<Integer> commonIds = new HashSet<>(user.getFriends());
    commonIds.retainAll(friend.getFriends());
    return commonIds.stream()
        .map(this::getOrThrow)
        .collect(Collectors.toList());
  }
}
