package ru.yandex.practicum.filmorate.service;

import jakarta.validation.Validation;
import org.junit.jupiter.api.BeforeEach;
import ru.yandex.practicum.filmorate.UserServiceTest;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

public class InMemoryUserServiceTest extends UserServiceTest<InMemoryUserStorage> {

  @Override
  @BeforeEach
  public void setUp() {
    userStorage = new InMemoryUserStorage();
    userService = new UserService(userStorage);
    factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }
}
