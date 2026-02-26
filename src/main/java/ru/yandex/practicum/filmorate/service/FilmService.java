package ru.yandex.practicum.filmorate.service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.exeption.NotFoundException;
import ru.yandex.practicum.filmorate.exeption.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

@Slf4j
@Service
public class FilmService {

  private final FilmStorage filmStorage;
  private final UserService userService;

  @Autowired
  public FilmService(@Qualifier("inMemoryFilmStorage") FilmStorage filmStorage, UserService userService) {
    this.filmStorage = filmStorage;
    this.userService = userService;
  }

  public Collection<Film> findAll() {
    return filmStorage.findAll();
  }

  public Film create(@RequestBody Film film) throws ValidationException {
    validateDescription(film);

    film = filmStorage.create(film);
    log.info("Добавлен новый фильм: id={}, name={}", film.getId(), film.getName());
    return film;
  }

  public Film update(@RequestBody Film newFilm) throws ValidationException, NotFoundException {
    Optional<Film> oldfilmOptional = filmStorage.findById(newFilm.getId());
    if (oldfilmOptional.isEmpty()) {
      log.warn("Попытка обновления несуществующего фильма: id={}", newFilm.getId());
      throw new NotFoundException("Фильм с Id " + newFilm.getId() + " не найден");
    }

    Film oldFilm = oldfilmOptional.get();
    // Обновление полей, если они не null
    if (newFilm.getName() != null && !newFilm.getName().isBlank()) {
      oldFilm.setName(newFilm.getName());
      log.debug("обновлено поле: Name, новое значение: {}", newFilm.getName());
    }

    if (newFilm.getDescription() != null) {
      validateDescription(newFilm);
      oldFilm.setDescription(newFilm.getDescription());
      log.debug("обновлено поле: Description, новое значение: {}", newFilm.getDescription());
    }

    if (newFilm.getReleaseDate() != null) {
      validateReleaseDate(newFilm);
      oldFilm.setReleaseDate(newFilm.getReleaseDate());
      log.debug("обновлено поле: ReleaseDate, новое значение: {}", newFilm.getReleaseDate());
    }

    if (newFilm.getDuration() != null) {
      validateDuration(newFilm);
      oldFilm.setDuration(newFilm.getDuration());
      log.debug("обновлено поле: Duration, новое значение: {}", newFilm.getDuration());
    }

    filmStorage.update(oldFilm);
    log.info("Обновлен фильм: {}", oldFilm.getName());
    return oldFilm;
  }

  public void addLike(int id, int userId) {
    Film film = getFilmOrThrow(id);
    userService.getUserOrThrow(userId);

    film.getLikes().add(userId);
    filmStorage.update(film);
  }

  public void removeLike(int filmId, int userId) {
    Film film = getFilmOrThrow(filmId);
    userService.getUserOrThrow(userId);

    boolean removed = film.getLikes().remove(userId);

    if (removed) {
      filmStorage.update(film);
      log.info("Пользователь id={} удалил лайк с фильма id={}", userId, filmId);
    } else {
      log.info("Лайк от пользователя id={} на фильме id={} не найден", userId, filmId);
    }
  }

  private Film getFilmOrThrow(int id) throws NotFoundException {

    return filmStorage.findById(id)
        .orElseThrow(() -> {
          log.warn("Не найден фильм: id = {}", id);
          return new NotFoundException("Фильм с Id " + id + " не найден");
        });
  }

  private void validateReleaseDate(Film film) throws ValidationException {
    if (film.getReleaseDate() == null || film.getReleaseDate()
        .isBefore(LocalDate.of(1895, 12, 28))
        && film.getReleaseDate().isBefore(LocalDate.now().plusDays(1))) {
      throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
    }
  }

  private void validateDescription(Film film) throws ValidationException {
    if (film.getDescription() != null && (film.getDescription().isBlank()
        || film.getDescription().length() > 200)) {
      log.warn("Фильм {} не прошел валидацию по полю: {}", film.getName(), film.getDescription());
      throw new ValidationException("Максимальная длина описания — 200 символов");
    }
  }

  private void validateDuration(Film film) throws ValidationException {
    if (film.getDuration() == null || !film.getDuration().isPositive()) {
      log.warn("Фильм {} не прошел валидацию по полю: Duration", film.getName());
      throw new ValidationException("Продолжительность фильма должна быть положительным числом");
    }
  }

  public Collection<Film> getPopularFilms(int count) {
    return filmStorage.findAll().stream()
        .sorted((f1, f2) -> f2.getLikes().size() - f1.getLikes().size())
        .limit(count)
        .collect(Collectors.toList());
  }
}
