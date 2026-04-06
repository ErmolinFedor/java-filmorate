package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.SearchType;
import ru.yandex.practicum.filmorate.model.SortBy;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FilmStorage {

  Collection<Film> findAll();

  Film create(@RequestBody Film film);

  Film update(@RequestBody Film newFilm);

  Optional<Film> findById(int id);

  void addLike(int filmId, int userId);

  void deleteLike(int filmId, int userId);

  Collection<Film> getPopular(int count);

  Collection<Film> getFilmsByDirector(int directorId, SortBy sortBy);

  Collection<Film> searchByDirectorAndName(String query, List<SearchType> by);
}
