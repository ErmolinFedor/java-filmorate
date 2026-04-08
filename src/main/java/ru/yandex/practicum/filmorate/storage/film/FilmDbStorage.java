package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.BaseRepository;
import ru.yandex.practicum.filmorate.model.*;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

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
  private static final String GET_POPULAR_WITH_FILTERS_QUERY =
          "SELECT f.*, f.duration AS duration_seconds, m.name AS mpa_name FROM films f "
                  + "LEFT JOIN mpa_ratings m ON f.mpa_id = m.id "
                  + "LEFT JOIN likes l ON f.id = l.id_film "
                  + "LEFT JOIN film_genres fg ON f.id = fg.film_id "
                  + "WHERE (? IS NULL OR fg.genre_id = ?) "
                  + "AND (? IS NULL OR EXTRACT(YEAR FROM f.release_date) = ?) "
                  + "GROUP BY f.id, m.name "
                  + "ORDER BY COUNT(l.id_user) DESC LIMIT ?";
  private static final String GET_BY_DIRECTORY_SORT_BY_YEAR_QUERY =
      "SELECT \n" +
          "    f.*, f.duration AS duration_seconds, m.name AS mpa_name\n" +
          "FROM films AS f\n" +
          "JOIN film_directors AS fd ON f.id = fd.film_id\n" +
          "LEFT JOIN mpa_ratings m ON f.mpa_id = m.id\n" +
          "WHERE fd.director_id = ?\n" +
          "ORDER BY f.release_date ASC;";

  private static final String GET_BY_DIRECTORY_SORT_BY_LIKES_QUERY =
      "SELECT \n" +
          "    f.*, f.duration AS duration_seconds, \n" +
          "    COUNT(l.id_user) AS count_likes," +
          "    m.name AS mpa_name\n" +
          "FROM films AS f\n" +
          "JOIN film_directors AS fd ON f.id = fd.film_id\n" +
          "LEFT JOIN likes AS l ON f.id = l.id_film\n" +
          "LEFT JOIN mpa_ratings m ON f.mpa_id = m.id\n" +
          "WHERE fd.director_id = ?\n" +
          "GROUP BY f.id\n" +
          "ORDER BY count_likes DESC;";

  private static final String SEARCH_BY_DIRECTORY_AND_NAME_QUERY =
      "SELECT f.*, m.name AS mpa_name, f.duration AS duration_seconds FROM films f " +
          "LEFT JOIN mpa_ratings m ON f.mpa_id = m.id " +
          "LEFT JOIN film_directors fd ON f.id = fd.film_id " +
          "LEFT JOIN directors d ON fd.director_id = d.id " +
          "WHERE ";
  private static final String GET_COMMON_FILMS_QUERY =
          "SELECT f.*, f.duration AS duration_seconds, m.name AS mpa_name " +
                  "FROM films f " +
                  "LEFT JOIN mpa_ratings m ON f.mpa_id = m.id " +
                  "LEFT JOIN likes l ON f.id = l.id_film " +
                  "WHERE f.id IN ( " +
                  "    SELECT l1.id_film " +
                  "    FROM likes l1 " +
                  "    INNER JOIN likes l2 ON l1.id_film = l2.id_film " +
                  "    WHERE l1.id_user = ? AND l2.id_user = ? " +
                  ") " +
                  "GROUP BY f.id, m.name " +
                  "ORDER BY COUNT(l.id_user) DESC, f.id ASC";


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
    addGenres(film);
    addDirectors(film);
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
    addGenres(newFilm);
    jdbc.update("DELETE FROM film_directors WHERE film_id = ?", newFilm.getId());
    addDirectors(newFilm);

    return newFilm;
  }

  private void addDirectors(Film newFilm) {
    if (newFilm.getDirectors() != null && !newFilm.getDirectors().isEmpty()) {
      List<Integer> directorIds = newFilm.getDirectors().stream()
          .map(Director::getId)
          .distinct()
          .collect(Collectors.toList());

      jdbc.batchUpdate(
          "INSERT INTO film_directors (film_id, director_id) VALUES (?, ?)",
          new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
              ps.setInt(1, newFilm.getId());
              ps.setInt(2, directorIds.get(i));
            }

            @Override
            public int getBatchSize() {
              return directorIds.size();
            }
          }
      );
    }
  }

  private void addGenres(Film newFilm) {
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
  }

  @Override
  public Optional<Film> findById(int id) {
    Optional<Film> filmOpt = findOne(FIND_BY_ID_QUERY, id);
    filmOpt.ifPresent(film -> {
      loadGenres(film);
      loadDirectors(film);
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
  public Collection<Film> getPopular(int count, Integer genreId, Integer year) {
    List<Film> films = jdbc.query(GET_POPULAR_WITH_FILTERS_QUERY, mapper,
            genreId, genreId, year, year, count);

    if (films.isEmpty()) {
      return films;
    }

    List<Integer> filmIds = films.stream()
            .map(Film::getId)
            .collect(Collectors.toList());

    String inSql = String.join(",", Collections.nCopies(filmIds.size(), "?"));

    enrichByLikes(films, filmIds, inSql);
    enrichByGenres(films, filmIds, inSql);

    return films;
  }

  @Override
  public Collection<Film> getFilmsByDirector(int directorId, SortBy sortBy) {
    if (sortBy.equals(SortBy.year))
      return getFilmsByDirectorAndYear(directorId);
    else
      return getFilmsByDirectorAndLikes(directorId);
  }

  @Override
  public Collection<Film> searchByDirectorAndName(String query, List<SearchType> by) {
    String searchPattern = "%" + query.toLowerCase() + "%";
    StringBuilder sql = new StringBuilder(SEARCH_BY_DIRECTORY_AND_NAME_QUERY);
    List<Object> params = new ArrayList<>();

    List<String> conditions = new ArrayList<>();
    if (by.contains(SearchType.title)) {
      conditions.add("LOWER(f.name) LIKE ?");
      params.add(searchPattern);
    }
    if (by.contains(SearchType.director)) {
      conditions.add("LOWER(d.name) LIKE ?");
      params.add(searchPattern);
    }
    sql.append(String.join(" OR ", conditions));
    sql.append(" GROUP BY f.id");

    List<Film> films = jdbc.query(sql.toString(), mapper, params.toArray());

    List<Integer> filmIds = films.stream().map(Film::getId).collect(Collectors.toList());
    String inSql = String.join(",", Collections.nCopies(filmIds.size(), "?"));

    enrichByLikes(films, filmIds, inSql);
    enrichByGenres(films, filmIds, inSql);
    enrichByDirectors(films, filmIds, inSql);
    return films;
  }

  private Collection<Film> getFilmsByDirectorAndLikes(int directorId) {
    List<Film> films = jdbc.query(GET_BY_DIRECTORY_SORT_BY_LIKES_QUERY, mapper, directorId);

    if (films.isEmpty()) {
      return films;
    }

    List<Integer> filmIds = films.stream()
        .map(Film::getId)
        .collect(Collectors.toList());

    String inSql = String.join(",", Collections.nCopies(filmIds.size(), "?"));
    enrichByLikes(films, filmIds, inSql);
    enrichByGenres(films, filmIds, inSql);
    enrichByDirectors(films, filmIds, inSql);

    return films;
  }

  private void enrichByLikes(List<Film> films, List<Integer> filmIds, String inSql) {
    String sql = "SELECT id_film, id_user FROM likes WHERE id_film IN (" + inSql + ")";

    jdbc.query(sql, (rs) -> {
      int filmId = rs.getInt("id_film");
      int userId = rs.getInt("id_user");

      films.stream()
          .filter(f -> f.getId() == filmId)
          .findFirst()
          .ifPresent(f -> f.getLikes().add(userId));
    }, filmIds.toArray());
  }

  private void enrichByGenres(List<Film> films, List<Integer> filmIds, String inSql) {
    String sql = "SELECT fg.film_id, fg.genre_id, g.name " +
        "FROM film_genres fg " +
        "JOIN genres g ON g.id = fg.genre_id " +
        "WHERE fg.film_id IN (" + inSql + ")";

    jdbc.query(sql, (rs) -> {
      int filmId = rs.getInt("film_id");
      int genreId = rs.getInt("genre_id");
      String genreName = rs.getString("name");

      films.stream()
          .filter(f -> f.getId() == filmId)
          .findFirst()
          .ifPresent(f -> f.getGenres().add(Genre.builder().id(genreId).name(genreName).build()));
    }, filmIds.toArray());
  }

  private void enrichByDirectors(List<Film> films, List<Integer> filmIds, String inSql) {
    String sqlByDirectors = "SELECT fd.film_id, fd.director_id, d.name " +
        "FROM film_directors fd " +
        "JOIN directors d ON d.id = fd.director_id " +
        "WHERE fd.film_id IN (" + inSql + ")";
    jdbc.query(sqlByDirectors, (rs) -> {
      int filmId = rs.getInt("film_id");
      int directorIdForFilm = rs.getInt("director_id");
      String directorNameForFilm = rs.getString("name");

      films.stream()
          .filter(f -> f.getId() == filmId)
          .findFirst()
          .ifPresent(f -> f.getDirectors().add(Director.builder().id(directorIdForFilm).name(directorNameForFilm).build()));
    }, filmIds.toArray());
  }

  private Collection<Film> getFilmsByDirectorAndYear(int directorId) {
    List<Film> films = jdbc.query(GET_BY_DIRECTORY_SORT_BY_YEAR_QUERY, mapper, directorId);

    if (films.isEmpty()) {
      return films;
    }

    List<Integer> filmIds = films.stream()
        .map(Film::getId)
        .collect(Collectors.toList());

    String inSql = String.join(",", Collections.nCopies(filmIds.size(), "?"));
    enrichByLikes(films, filmIds, inSql);
    enrichByGenres(films, filmIds, inSql);
    enrichByDirectors(films, filmIds, inSql);

    return films;
  }

  @Override
  public Collection<Film> getCommonFilms(int userId, int friendId) {
    List<Film> films = jdbc.query(GET_COMMON_FILMS_QUERY, mapper, userId, friendId);

    if (films.isEmpty()) {
      return films;
    }

    List<Integer> filmIds = films.stream()
            .map(Film::getId)
            .collect(Collectors.toList());

    String inSql = String.join(",", Collections.nCopies(filmIds.size(), "?"));

    enrichByLikes(films, filmIds, inSql);
    enrichByGenres(films, filmIds, inSql);
    enrichByDirectors(films, filmIds, inSql);

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

  private void loadDirectors(Film film) {
    String sql = "SELECT d.id, d.name FROM directors d " + "JOIN film_directors fd ON d.id = fd.director_id "
        + "WHERE fd.film_id = ? " + "ORDER BY d.id";

    List<Director> genres = jdbc.query(sql,
        (rs, rowNum) -> new Director(rs.getInt("id"), rs.getString("name")), film.getId());

    film.setDirectors(new LinkedHashSet<>(genres));
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

    String sqlByDirector = "SELECT fd.film_id, d.id AS director_id, d.name AS director_name " +
        "FROM film_directors fd " +
        "JOIN directors d ON fd.director_id = d.id " +
        "WHERE fd.film_id IN (" + inSql + ")";

    jdbc.query(sqlByDirector, (rs) -> {
      int filmId = rs.getInt("film_id");
      Director director = Director.builder()
          .id(rs.getInt("director_id"))
          .name(rs.getString("director_name"))
          .build();

      Film film = filmMap.get(filmId);
      if (film != null) {
        film.getDirectors().add(director);
      }
    }, filmMap.keySet().toArray());
  }
}
