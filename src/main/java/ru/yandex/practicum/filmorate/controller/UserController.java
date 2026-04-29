package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exeption.NotFoundException;
import ru.yandex.practicum.filmorate.exeption.ValidationException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

  private final UserService userService;

  private final FilmService filmService;

  @Autowired
  public UserController(UserService userService,
                        FilmService filmService) {
    this.userService = userService;
    this.filmService = filmService;
  }

  @GetMapping
  public Collection<User> findAll() {
    log.info("Получен запрос GET /users на получение списка всех пользователей");
    return userService.findAll();
  }

  @GetMapping("/{userId}")
  public User findById(@PathVariable int userId) throws NotFoundException {
    log.info("Получен запрос GET /users/userId на получение пользователя с id={}", userId);
    return userService.findById(userId);
  }

  @DeleteMapping("/{userId}")
  @ResponseStatus(NO_CONTENT)
  public void deleteUser(@PathVariable int userId) throws NotFoundException {
    log.info("Получен запрос DELETE /users/{} на удаление пользователя", userId);
    userService.delete(userId);
  }

  @GetMapping("/{userId}/friends")
  public Collection<User> findAllFriendsById(@PathVariable int userId) throws NotFoundException {
    log.info("Получен запрос GET /users/{id}/friends на получение списка друзей пользователя id={}",
        userId);
    return userService.findAllFriendsById(userId);
  }

  @GetMapping("/{userId}/friends/common/{otherId}")
  public Collection<User> getCommonFriends(@PathVariable int userId, @PathVariable int otherId)
      throws NotFoundException {
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
  public User update(@Valid @RequestBody User newUser) throws ValidationException, NotFoundException {
    log.info("Получен запрос PUT /users с телом: {}", newUser);
    return userService.update(newUser);
  }

  @PutMapping("/{id}/friends/{friendId}")
  @ResponseStatus(NO_CONTENT)
  public void addFriend(@PathVariable int id, @PathVariable int friendId)
      throws ValidationException, NotFoundException {
    log.info("Получен запрос PUT /users/{id}/friends/{friendId} с id: {} и friendId: {}", id,
        friendId);
    userService.addFriend(id, friendId);
  }

  @DeleteMapping("/{id}/friends/{friendId}")
  @ResponseStatus(NO_CONTENT)
  public void deleteFriend(@PathVariable int id, @PathVariable int friendId)
      throws ValidationException, NotFoundException {
    log.info("Получен запрос DELETE /users/{id}/friends/{friendId} с id: {} и friendId: {}", id,
        friendId);
    userService.deleteFriend(id, friendId);
  }

  @GetMapping("/{id}/feed")
  public Collection<Event> getFeed(@PathVariable Integer id) {
    return userService.getFeed(id);
  }

  @GetMapping("/{userId}/recommendations")
  public Collection<Film> getRecommendations(@PathVariable int userId) {
    log.info("Получен запрос GET /users/{}/recommendations", userId);
    return filmService.getRecommendations(userId);
  }

}
