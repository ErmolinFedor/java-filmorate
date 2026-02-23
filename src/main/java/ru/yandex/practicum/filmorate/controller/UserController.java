package ru.yandex.practicum.filmorate.controller;

import static org.springframework.http.HttpStatus.CREATED;

import jakarta.validation.Valid;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
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
}
