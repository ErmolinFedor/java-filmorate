package ru.yandex.practicum.filmorate.controller;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import jakarta.validation.Valid;
import java.util.Collection;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exeption.NotFoundException;
import ru.yandex.practicum.filmorate.exeption.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
  private final UserService userService;

  @Autowired
  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping
  public Collection<User> findAll() {
    log.info("Получен запрос GET /users на получение списка всех пользователей");
    return userService.findAll();
  }

  @GetMapping("/{userId}")
  public Optional<User> findById(@PathVariable int userId) throws NotFoundException {
    log.info("Получен запрос GET /users/userId на получение пользователя с id={}", userId);
    return userService.findById(userId);
  }

  @GetMapping("/{userId}/friends")
  public Collection<User> findAllFriendsById(@PathVariable int userId) throws NotFoundException {
    log.info(
        "Получен запрос GET /users/{id}/friends на получение списка друзей пользователя id={}",
        userId);
    return userService.findAllFriendsById(userId);
  }

  @GetMapping("/{userId}/friends/common/{otherId}")
  public Collection<User> getCommonFriends(@PathVariable int userId, @PathVariable int otherId) throws NotFoundException {
    log.info(
        "Получен запрос GET /users/{}/friends/common/{} на получение списка общих друзей с пользователем",
        userId, otherId);
    return userService.getCommonFriends(userId, otherId);
  }

  @PostMapping
  @ResponseStatus(CREATED)
  public User create(@Valid @RequestBody User user) throws ValidationException {
    log.info("Получен запрос POST /users с телом: {}", user);
    return userService.create(user);
  }

  @PutMapping
  public User update(@RequestBody User newUser) throws ValidationException, NotFoundException {
    log.info("Получен запрос PUT /users с телом: {}", newUser);
    return userService.update(newUser);
  }

  @PutMapping("/{id}/friends/{friendId}")
  @ResponseStatus(NO_CONTENT)
  public void addFriend(@PathVariable int id, @PathVariable int friendId) throws ValidationException, NotFoundException {
    log.info("Получен запрос PUT /users/{id}/friends/{friendId} с id: {} и friendId: {}", id, friendId);
    userService.addFriend(id, friendId);
  }

  @DeleteMapping("/{id}/friends/{friendId}")
  @ResponseStatus(NO_CONTENT)
  public void deleteFriend(@PathVariable int id, @PathVariable int friendId) throws ValidationException, NotFoundException {
    log.info("Получен запрос DELETE /users/{id}/friends/{friendId} с id: {} и friendId: {}", id, friendId);
    userService.deleteFriend(id, friendId);
  }
}
