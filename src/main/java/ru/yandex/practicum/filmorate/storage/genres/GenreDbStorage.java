package ru.yandex.practicum.filmorate.storage.genres;

import java.util.Collection;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.BaseRepository;
import ru.yandex.practicum.filmorate.model.Genre;

@Component
@Slf4j
public class GenreDbStorage extends BaseRepository<Genre> implements GenreStorage {

  private static final String FIND_ALL_QUERY = "SELECT * FROM genres ORDER BY id";
  private static final String FIND_BY_ID_QUERY = "SELECT * FROM genres WHERE id = ?";

  public GenreDbStorage(JdbcTemplate jdbc, RowMapper<Genre> mapper) {
    super(jdbc, mapper, Genre.class);
  }

  public Collection<Genre> findAll() {
    return findAll(FIND_ALL_QUERY);
  }

  public Optional<Genre> findById(int id) {
    return findOne(FIND_BY_ID_QUERY, id);
  }
}
