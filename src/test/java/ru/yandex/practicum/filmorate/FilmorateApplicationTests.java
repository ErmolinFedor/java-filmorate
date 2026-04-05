package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.DirectorController;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class FilmorateApplicationTests {

  @Autowired
  private UserController userController;
  @Autowired
  private FilmController filmController;
  @Autowired
  private DirectorController directorController;
  @Autowired
  private UserService userService;
  @Autowired
  private FilmService filmService;

  @Test
  void contextLoads() {
    assertThat(userController).isNotNull();
    assertThat(filmController).isNotNull();
    assertThat(directorController).isNotNull();
    assertThat(userService).isNotNull();
		assertThat(filmService).isNotNull();
	}

}
