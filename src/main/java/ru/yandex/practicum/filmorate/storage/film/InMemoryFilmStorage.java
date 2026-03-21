package ru.yandex.practicum.filmorate.storage.film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
  private final Map<Integer, Film> films = new HashMap<>();
  private int idCounter = 0;

  @Override
  public Collection<Film> findAll() {
    return films.values();
  }

  @Override
  public Film create(Film film)  {
    film.setId(++idCounter);
    films.put(film.getId(), film);
    return film;
  }

  @Override
  public Film update(Film film) {
    films.put(film.getId(), film);
    return film;
  }

  @Override
  public Optional<Film> findById(int id) {
    return Optional.ofNullable(films.get(id));
  }

  @Override
  public void addLike(int filmId, int userId) {
    Film film = films.get(filmId);
    if (film != null) {
      film.getLikes().add(userId);
    }
  }

  @Override
  public void deleteLike(int filmId, int userId) {
    Film film = films.get(filmId);
    if (film != null) {
      film.getLikes().remove(userId);
    }
  }

  @Override
  public Collection<Film> getPopular(int count) {
    return films.values().stream()
        .sorted((f1, f2) -> f2.getLikes().size() - f1.getLikes().size())
        .limit(count)
        .collect(Collectors.toList());
  }
}
