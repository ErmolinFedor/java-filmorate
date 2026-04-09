package ru.yandex.practicum.filmorate.service;

import static ru.yandex.practicum.filmorate.model.EventType.LIKE;
import static ru.yandex.practicum.filmorate.model.Operation.ADD;
import static ru.yandex.practicum.filmorate.model.Operation.REMOVE;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.exeption.NotFoundException;
import ru.yandex.practicum.filmorate.exeption.ValidationException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.feed.FeedStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genres.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {

  private final FilmStorage filmStorage;
  private final UserStorage userStorage;
  private final MpaStorage mpaStorage;
  private final GenreStorage genreStorage;
  private final DirectorStorage directorStorage;
  private final FeedStorage feedStorage;

  @Autowired
  public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                     @Qualifier("userDbStorage") UserStorage userStorage,
                     @Qualifier("mpaDbStorage") MpaStorage mpaStorage,
                     @Qualifier("genreDbStorage") GenreStorage genreStorage,
                     @Qualifier("directorDbStorage") DirectorStorage directorStorage,
                     @Qualifier("feedDbStorage") FeedStorage feedStorage) {
    this.filmStorage = filmStorage;
    this.userStorage = userStorage;
    this.mpaStorage = mpaStorage;
    this.genreStorage = genreStorage;
    this.directorStorage = directorStorage;
    this.feedStorage = feedStorage;
  }

  public Collection<Film> findAll() {
    return filmStorage.findAll();
  }

  public Film create(@RequestBody Film film) throws ValidationException {
    validateDescription(film);

    if (film.getMpa() != null) {
      int mpaId = film.getMpa().getId();
      mpaStorage.findById(mpaId)
          .orElseThrow(() -> new NotFoundException("Рейтинг MPA с id=" + mpaId + " не найден"));
    }

    if (film.getGenres() != null && !film.getGenres().isEmpty()) {
      List<Integer> genreIds = film.getGenres().stream()
          .map(Genre::getId)
          .distinct()
          .collect(Collectors.toList());

      List<Genre> existingGenres = genreStorage.findAllByIds(genreIds);

      if (existingGenres.size() != genreIds.size()) {
        throw new NotFoundException("Один или несколько жанров не найдены в базе данных");
      }
    }

    if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
      List<Integer> directorIds = film.getDirectors().stream()
          .map(Director::getId)
          .distinct()
          .toList();

      List<Director> existingDirectors = directorStorage.findAllByIds(directorIds);

      if (existingDirectors.size() != directorIds.size()) {
        throw new NotFoundException("Один или несколько режиссёров не найдены в базе данных");
      }
    }

    return filmStorage.create(film);
  }

  public Film update(@RequestBody Film newFilm) throws ValidationException, NotFoundException {

    Film oldFilm = getFilmOrThrow(newFilm.getId());
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

    if (newFilm.getGenres() != null && !newFilm.getGenres().isEmpty()) {
      List<Integer> genreIds = newFilm.getGenres().stream()
          .map(Genre::getId)
          .distinct()
          .collect(Collectors.toList());

      List<Genre> existingGenres = genreStorage.findAllByIds(genreIds);

      if (existingGenres.size() != genreIds.size()) {
        throw new NotFoundException("Один или несколько жанров не найдены в базе данных");
      }
      oldFilm.setGenres(newFilm.getGenres());
    }

    if (newFilm.getDirectors() != null) {
      List<Integer> directorIds = newFilm.getDirectors().stream()
          .map(Director::getId)
          .distinct()
          .toList();

      List<Director> existingDirectors = directorStorage.findAllByIds(directorIds);

      if (existingDirectors.size() != directorIds.size()) {
        throw new NotFoundException("Один или несколько режиссёров не найдены в базе данных");
      }

      oldFilm.setDirectors(newFilm.getDirectors());
      log.debug("обновлено поле: Director, новое значение: {}", newFilm.getDirectors());
    }

    filmStorage.update(oldFilm);
    log.info("Обновлен фильм: {}", oldFilm.getName());
    return oldFilm;
  }

  public void addLike(int id, int userId) throws NotFoundException {
    getFilmOrThrow(id);
    checkUserExists(userId);

    filmStorage.addLike(id, userId);
    log.info("Пользователь {} поставил лайк фильму {}", userId, id);
    feedStorage.addEvent(userId, id, LIKE, ADD);
  }

  public void removeLike(int filmId, int userId) throws NotFoundException {
    getFilmOrThrow(filmId);
    checkUserExists(userId);

    filmStorage.deleteLike(filmId, userId);
    log.info("Пользователь {} удалил лайк с фильма {}", userId, filmId);
    feedStorage.addEvent(userId, filmId, LIKE, REMOVE);
  }

  private Film getFilmOrThrow(int id) throws NotFoundException {

    return filmStorage.findById(id).orElseThrow(() -> {
      log.warn("Не найден фильм: id = {}", id);
      return new NotFoundException("Фильм с Id " + id + " не найден");
    });
  }

  private void validateReleaseDate(Film film) throws ValidationException {
    if (film.getReleaseDate() == null
        || film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28)) && film.getReleaseDate()
        .isBefore(LocalDate.now().plusDays(1))) {
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

  public Collection<Film> getPopularFilms(int count, Integer genreId, Integer year) throws ValidationException {
    if (count < 0) {
      log.error("Передано некорректное значение count: {}", count);
      throw new ValidationException("Количество фильмов (count) должно быть положительным числом.");
    }

    return filmStorage.getPopular(count, genreId, year);
  }

  public Collection<Film> getCommonFilms(int userId, int friendId) {
    checkUserExists(userId);
    checkUserExists(friendId);

    return filmStorage.getCommonFilms(userId, friendId);
  }


  private void checkUserExists(int id) throws NotFoundException {
    userStorage.findById(id).orElseThrow(() -> {
      log.warn("Не найден пользователь: id = {}", id);
      return new NotFoundException("Пользователь с Id " + id + " не найден");
    });
  }

  public Film getFilmById(int id) {
    return filmStorage.findById(id)
        .orElseThrow(() -> new NotFoundException("Фильм с id=" + id + " не найден"));
  }

  public Collection<Film> getFilmsByDirector(int directorId, SortBy sortBy) {
    return filmStorage.getFilmsByDirector(directorId, sortBy);
  }

  public Collection<Film> searchByDirectorAndName(String query, List<SearchType> by) {
    return filmStorage.searchByDirectorAndName(query, by);
  }
}
