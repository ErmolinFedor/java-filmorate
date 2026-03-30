package ru.yandex.practicum.filmorate.storage.film;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
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
    Collection<Film> films = findAll(FIND_ALL_QUERY);
    enrichFilms(films);
    return films;
  }

  @Override
  public Film create(Film film) {
    int id = insert(INSERT_QUERY,
        film.getName(),
        film.getDescription(),
        film.getReleaseDate(),
        film.getDuration().toSeconds(),
        film.getMpa().getId()
    );
    film.setId(id);

    if (film.getGenres() != null && !film.getGenres().isEmpty()) {
      List<Integer> genreIds = film.getGenres().stream()
          .map(Genre::getId)
          .distinct()
          .collect(Collectors.toList());

      jdbc.batchUpdate(
          "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)",
          new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
              ps.setInt(1, id);
              ps.setInt(2, genreIds.get(i));
            }

            @Override
            public int getBatchSize() {
              return genreIds.size();
            }
          }
      );
    }
    return film;
  }

  @Override
  public Film update(Film newFilm) {
    update(UPDATE_QUERY,
        newFilm.getName(),
        newFilm.getDescription(),
        newFilm.getReleaseDate(),
        newFilm.getDuration().toSeconds(),
        newFilm.getMpa().getId(),
        newFilm.getId()
    );

    jdbc.update("DELETE FROM film_genres WHERE film_id = ?", newFilm.getId());

    if (newFilm.getGenres() != null && !newFilm.getGenres().isEmpty()) {
      List<Integer> genreIds = newFilm.getGenres().stream()
          .map(Genre::getId)
          .distinct()
          .collect(Collectors.toList());

      jdbc.batchUpdate(
          "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)",
          new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
              ps.setInt(1, newFilm.getId());
              ps.setInt(2, genreIds.get(i));
            }

            @Override
            public int getBatchSize() {
              return genreIds.size();
            }
          }
      );
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
    List<Film> films = jdbc.query(GET_POPULAR_QUERY, mapper, count);

    if (films.isEmpty()) {
      return films;
    }

    List<Integer> filmIds = films.stream()
        .map(Film::getId)
        .collect(Collectors.toList());

    String inSql = String.join(",", Collections.nCopies(filmIds.size(), "?"));
    String sql = "SELECT id_film, id_user FROM likes WHERE id_film IN (" + inSql + ")";

    jdbc.query(sql, (rs) -> {
      int filmId = rs.getInt("id_film");
      int userId = rs.getInt("id_user");

      films.stream()
          .filter(f -> f.getId() == filmId)
          .findFirst()
          .ifPresent(f -> f.getLikes().add(userId));
    }, filmIds.toArray());

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

  private void enrichFilms(Collection<Film> films) {
    if (films == null || films.isEmpty()) return;

    Map<Integer, Film> filmMap = films.stream()
        .collect(Collectors.toMap(Film::getId, f -> f));

    String inSql = String.join(",", Collections.nCopies(films.size(), "?"));
    String sql = "SELECT fg.film_id, g.id AS genre_id, g.name AS genre_name " +
        "FROM film_genres fg " +
        "JOIN genres g ON fg.genre_id = g.id " +
        "WHERE fg.film_id IN (" + inSql + ")";

    jdbc.query(sql, (rs) -> {
      int filmId = rs.getInt("film_id");
      Genre genre = Genre.builder()
          .id(rs.getInt("genre_id"))
          .name(rs.getString("genre_name"))
          .build();

      Film film = filmMap.get(filmId);
      if (film != null) {
        film.getGenres().add(genre);
      }
    }, filmMap.keySet().toArray());
  }
}
