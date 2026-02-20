package ru.yandex.practicum.filmorate.controller;

import static org.springframework.http.HttpStatus.CREATED;

import jakarta.validation.Valid;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exeption.NotFoundException;
import ru.yandex.practicum.filmorate.exeption.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
  private final FilmService filmService;

  public FilmController(FilmService filmService) {
    this.filmService = filmService;
  }

  @GetMapping
  public Collection<Film> findAll() {
    log.info("Получен запрос GET /films на получение всех фильмов");
    return filmService.findAll();
  }

  @PostMapping
  @ResponseStatus(CREATED)
  public Film create(@Valid @RequestBody Film film) throws ValidationException {
    log.info("Получен запрос POST /films на добавление фильма: {}", film.getName());
    return filmService.create(film);
  }

  @PutMapping
  public Film update(@RequestBody Film film) throws ValidationException, NotFoundException {
    log.info("Получен запрос PUT /films на обновление фильма с id={}", film.getId());
    return filmService.update(film);
  }
}
