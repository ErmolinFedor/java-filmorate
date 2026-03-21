package ru.yandex.practicum.filmorate.storage.film;

import java.util.Collection;
import java.util.Optional;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.model.Film;

public interface FilmStorage {

  Collection<Film> findAll();

  Film create(@RequestBody Film film);

  Film update(@RequestBody Film newFilm);

  Optional<Film> findById(int id);

  void addLike(int filmId, int userId);

  void deleteLike(int filmId, int userId);

  Collection<Film> getPopular(int count);
}
