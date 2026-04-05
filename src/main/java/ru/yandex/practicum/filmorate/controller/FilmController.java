package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exeption.NotFoundException;
import ru.yandex.practicum.filmorate.exeption.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.SortBy;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

import static org.springframework.http.HttpStatus.CREATED;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

  private final FilmService filmService;

  @Autowired
  public FilmController(FilmService filmService) {
    this.filmService = filmService;
  }

  @GetMapping
  public Collection<Film> findAll() {
    log.info("Получен запрос GET /films на получение всех фильмов");
    return filmService.findAll();
  }

  @GetMapping("/{id}")
  public Film findById(@PathVariable int id) {
    log.info("Получен запрос GET /films/{}", id);
    return filmService.getFilmById(id);
  }

  @GetMapping("/popular")
  public Collection<Film> getPopularFilms(@RequestParam(defaultValue = "10") int count)
      throws ValidationException {
    log.info("Получен запрос GET /films/popular?count={count} на получение топ фильмов по лайкам");
    return filmService.getPopularFilms(count);
  }

  @GetMapping("/director/{directorId}")
  public Collection<Film> getFilmsByDirector(@PathVariable int directorId, @RequestParam(defaultValue = "year") SortBy sortBy)
      throws ValidationException {
    log.info("Получен запрос GET /films/director/{directorId}?sortBy=[year,likes] на получение фильмов" +
        " режиссера отсортированных по количеству лайков или году выпуска");
    return filmService.getFilmsByDirector(directorId, sortBy);
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

  @PutMapping("/{id}/like/{userId}")
  public void addLike(@PathVariable int id, @PathVariable int userId) {
    log.info("Получен запрос PUT /films/{}/like/{}, пользователь ставит лайк фильму", id, userId);
    filmService.addLike(id, userId);
  }

  @DeleteMapping("/{id}/like/{userId}")
  public void removeLike(@PathVariable int id, @PathVariable int userId) {
    log.info("Получен запрос DELETE /films/{}/like/{}, пользователь удаляет лайк", id, userId);
    filmService.removeLike(id, userId);
  }
}
