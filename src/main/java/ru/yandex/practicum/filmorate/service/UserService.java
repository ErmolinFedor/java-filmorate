package ru.yandex.practicum.filmorate.service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
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
    Optional<User> oldUserOptional = userStorage.findById(newUser.getId());
    if (oldUserOptional.isEmpty()) {
      log.warn("Ошибка обновления: пользователь с id={} не найден", newUser.getId());
      throw new NotFoundException("Пользователь с Id " + newUser.getId() + " не найден");
    }
    User oldUser = oldUserOptional.get();
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

  private void validateLogin(User user) throws ValidationException {
    if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
      log.error("Пользователь {} не прошел валидацию по полю: {}", user.getName(),
          user.getLogin());
      throw new ValidationException("Логин не может быть пустым и содержать пробелы");
    }
  }
}
