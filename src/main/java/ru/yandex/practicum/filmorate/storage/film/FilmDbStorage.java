package ru.yandex.practicum.filmorate.storage.film;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.BaseRepository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

@Component
@Slf4j
public class FilmDbStorage extends BaseRepository<Film> implements FilmStorage {

  private static final String FIND_ALL_QUERY =
      "SELECT f.*, f.duration AS duration_seconds, m.name AS mpa_name FROM films f "
          + "LEFT JOIN mpa_ratings m ON f.mpa_id = m.id";
  private static final String FIND_BY_ID_QUERY =
      "SELECT f.*, f.duration AS duration_seconds, "
          + "m.name AS mpa_name "
          + "FROM films f LEFT JOIN mpa_ratings m ON f.mpa_id = m.id WHERE f.id = ?";
  private static final String INSERT_QUERY =
      "INSERT INTO films(name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
  private static final String UPDATE_QUERY =
      "UPDATE films SET name = ?, description = ?, release_date = ?, "
          + "duration = ?, mpa_id = ? WHERE id = ?";
  private static final String INSERT_LIKE_QUERY =
      "INSERT INTO likes (id_user, id_film) VALUES (?, ?)";
  private static final String DELETE_LIKE_QUERY =
      "DELETE FROM likes WHERE id_user = ? AND id_film = ?";
  private static final String GET_POPULAR_QUERY =
      "SELECT f.*, f.duration AS duration_seconds, m.name AS mpa_name FROM films f "
          + "LEFT JOIN mpa_ratings m ON f.mpa_id = m.id LEFT JOIN likes l ON f.id = l.id_film "
          + "GROUP BY f.id, m.name ORDER BY COUNT(l.id_user) DESC LIMIT ?";


  public FilmDbStorage(JdbcTemplate jdbc, RowMapper<Film> mapper) {
    super(jdbc, mapper, Film.class);
  }

  @Override
  public Collection<Film> findAll() {
    return findAll(FIND_ALL_QUERY);
  }

  @Override
  public Film create(Film film) {

    int id = insert(INSERT_QUERY, film.getName(), film.getDescription(), film.getReleaseDate(),
        film.getDuration().toSeconds(), film.getMpa().getId());
    film.setId(id);
    if (film.getGenres() != null && !film.getGenres().isEmpty()) {
      Set<Integer> uniqueGenreIds = film.getGenres().stream().map(Genre::getId)
          .collect(Collectors.toSet());

      for (Integer genreId : uniqueGenreIds) {
        jdbc.update("INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)", id, genreId);
      }
    }
    return film;
  }

  @Override
  public Film update(Film newFilm) {
    update(UPDATE_QUERY, newFilm.getName(), newFilm.getDescription(), newFilm.getReleaseDate(),
        newFilm.getDuration().toSeconds(), newFilm.getMpa().getId(), newFilm.getId());
    jdbc.update("DELETE FROM film_genres WHERE film_id = ?", newFilm.getId());

    if (newFilm.getGenres() != null && !newFilm.getGenres().isEmpty()) {
      Set<Integer> uniqueGenreIds = newFilm.getGenres().stream().map(Genre::getId)
          .collect(Collectors.toSet());

      for (Integer genreId : uniqueGenreIds) {
        jdbc.update("INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)", newFilm.getId(),
            genreId);
      }
    }

    return newFilm;
  }

  @Override
  public Optional<Film> findById(int id) {
    Optional<Film> filmOpt = findOne(FIND_BY_ID_QUERY, id);
    filmOpt.ifPresent(film -> {
      loadGenres(film);
      loadLikes(film);
    });
    return filmOpt;
  }

  @Override
  public void addLike(int filmId, int userId) {
    jdbc.update(INSERT_LIKE_QUERY, userId, filmId);
  }

  @Override
  public void deleteLike(int filmId, int userId) {
    jdbc.update(DELETE_LIKE_QUERY, userId, filmId);
  }

  @Override
  public Collection<Film> getPopular(int count) {
    Collection<Film> films = jdbc.query(GET_POPULAR_QUERY, mapper, count);
    for (Film film : films) {
      loadLikes(film);
    }
    return films;
  }

  private void loadLikes(Film film) {
    String sql = "SELECT id_user FROM likes WHERE id_film = ?";
    List<Integer> userIds = jdbc.query(sql, (rs, rowNum) -> rs.getInt("id_user"), film.getId());
    film.getLikes().clear();
    film.getLikes().addAll(userIds);
  }

  private void loadGenres(Film film) {
    String sql = "SELECT g.id, g.name FROM genres g " + "JOIN film_genres fg ON g.id = fg.genre_id "
        + "WHERE fg.film_id = ? " + "ORDER BY g.id";

    List<Genre> genres = jdbc.query(sql,
        (rs, rowNum) -> new Genre(rs.getInt("id"), rs.getString("name")), film.getId());

    film.setGenres(new LinkedHashSet<>(genres));
  }
}
