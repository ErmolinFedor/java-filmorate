package ru.yandex.practicum.filmorate.service;

import static ru.yandex.practicum.filmorate.utils.Utils.getNextId;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.exeption.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

@Slf4j
@Service
public class FilmService {

  private final Map<Integer, Film> films = new HashMap<>();

  public Collection<Film> findAll() {
    return films.values();
  }

  public Film create(@RequestBody Film film) throws ValidationException {
    validateFilm(film);

    film.setId(getNextId(films));
    films.put(film.getId(), film);
    log.info("Добавлен новый фильм: id={}, name={}", film.getId(), film.getName());
    return film;
  }

  public Film update(@RequestBody Film newFilm) throws ValidationException {
    if (!films.containsKey(newFilm.getId())) {
      log.warn("Попытка обновления несуществующего фильма: id={}", newFilm.getId());
      throw new ValidationException("Фильм с Id " + newFilm.getId() + " не найден");
    }

    Film oldFilm = films.get(newFilm.getId());
    // Обновление полей, если они не null
    if (newFilm.getName() != null) {
      oldFilm.setName(newFilm.getName());
      log.debug("обновлено поле: {}", newFilm.getName());
    }

    if (newFilm.getDescription() != null) {
      oldFilm.setDescription(newFilm.getDescription());
      log.debug("обновлено поле: {}", newFilm.getDescription());
    }

    if (newFilm.getReleaseDate() != null) {
      oldFilm.setReleaseDate(newFilm.getReleaseDate());
      log.debug("обновлено поле: {}", newFilm.getReleaseDate());
    }

    if (newFilm.getDuration() != null) {
      oldFilm.setDuration(newFilm.getDuration());
      log.debug("обновлено поле: {}", newFilm.getDuration());
    }

    log.info("Обновлен фильм: {}", oldFilm.getName());
    validateFilm(oldFilm);
    return oldFilm;
  }


  private void validateFilm(Film film) throws ValidationException {
    log.trace("Старт валидации фильма: {}", film.getName());

    if (film.getReleaseDate() != null && film.getReleaseDate()
        .isBefore(LocalDate.of(1895, 12, 28))) {
      throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
    }

    if (film.getDescription() == null || film.getDescription().isBlank()
        || film.getDescription().length() > 200) {
      log.warn("Фильм {} не прошел валидацию по полю: {}", film.getName(), film.getDescription());
      throw new ValidationException("Максимальная длина описания — 200 символов");
    }

    if (film.getDuration() == null || film.getDuration().isNegative()) {
      log.warn("Фильм {} не прошел валидацию по полю: Duration", film.getName());
      throw new ValidationException("Продолжительность фильма должна быть положительным числом");
    }
  }
}
