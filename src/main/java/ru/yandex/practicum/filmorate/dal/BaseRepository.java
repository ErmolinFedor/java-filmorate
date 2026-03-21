package ru.yandex.practicum.filmorate.dal;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import ru.yandex.practicum.filmorate.exeption.InternalServerException;

@RequiredArgsConstructor
public class BaseRepository<T>  {
  protected final JdbcTemplate jdbc;
  protected final RowMapper<T> mapper;
  private final Class<T> entityType;

  protected Optional<T> findOne(String query, Object... params) {
    try {
      T result = jdbc.queryForObject(query, mapper, params);
      return Optional.ofNullable(result);
    } catch (EmptyResultDataAccessException ignored) {
      return Optional.empty();
    }
  }

  protected Collection<T> findAll(String query, Object... params) {
    return jdbc.query(query, mapper, params);
  }

  protected int insert(String query, Object... params) {
    GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
    jdbc.update(connection -> {
      PreparedStatement ps = connection
          .prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
      for (int idx = 0; idx < params.length; idx++) {
        ps.setObject(idx + 1, params[idx]);
      }
      return ps;
    }, keyHolder);

    Integer id = keyHolder.getKeyAs(Integer.class);
    if (id != null) {
      return id;
    } else {
      throw new InternalServerException("Не удалось сохранить данные");
    }
  }

  protected void update(String query, Object... params) {
    int rowsUpdated = jdbc.update(query, params);
    if (rowsUpdated == 0) {
      throw new InternalServerException("Не удалось обновить данные");
    }
  }
}
