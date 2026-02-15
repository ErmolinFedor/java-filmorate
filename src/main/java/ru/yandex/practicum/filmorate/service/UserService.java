package ru.yandex.practicum.filmorate.service;

import static ru.yandex.practicum.filmorate.utils.Utils.getNextId;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.exeption.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

@Service
public class UserService {

  private static final Logger logger = LoggerFactory.getLogger(UserService.class);
  private final Map<Integer, User> users = new HashMap<>();

  public Collection<User> findAll() {
    return users.values();
  }

  public User create(@RequestBody User user) throws ValidationException {
    validateLogin(user);

    if (user.getName() == null || user.getName().isBlank()) {
      user.setName(user.getLogin());
    }

    user.setId(getNextId(users));
    users.put(user.getId(), user);
    logger.info("Добавлен пользователь: id={}, login={}", user.getId(), user.getLogin());
    return user;
  }

  public User update(@RequestBody User newUser) throws ValidationException {
    if (!users.containsKey(newUser.getId())) {
      logger.warn("Ошибка обновления: пользователь с id={} не найден", newUser.getId());
      throw new ValidationException("Пользователь с Id " + newUser.getId() + " не найден");
    }
    User oldUser = users.get(newUser.getId());
    if (newUser.getBirthday() != null) {
      oldUser.setBirthday(newUser.getBirthday());
      logger.trace("обновлено поле: {}", newUser.getBirthday());
    }

    if (newUser.getLogin() != null) {
      validateLogin(newUser);
      oldUser.setLogin(newUser.getLogin());
      logger.trace("обновлено поле: {}", newUser.getLogin());
    }

    if (newUser.getName() != null && !newUser.getName().isBlank()) {
      oldUser.setName(newUser.getName());
      logger.debug("обновлено поле: {}", newUser.getName());
    }

    if (newUser.getEmail() != null) {
      oldUser.setEmail(newUser.getEmail());
      logger.debug("обновлено поле: {}", newUser.getEmail());
    }

    logger.info("Пользователь id={} успешно обновлен", newUser.getId());
    return oldUser;
  }

  private void validateLogin(User user) throws ValidationException {
    if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
      logger.error("Пользователь {} не прошел валидацию по полю: {}", user.getName(),
          user.getLogin());
      throw new ValidationException("Логин не может быть пустым и содержать пробелы");
    }
  }
}
