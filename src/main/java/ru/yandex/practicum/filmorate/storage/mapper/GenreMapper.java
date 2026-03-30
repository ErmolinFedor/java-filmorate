package ru.yandex.practicum.filmorate.storage.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;

@Component
public class GenreMapper implements RowMapper<Genre> {

  @Override
  public Genre mapRow(ResultSet rs, int rowNum) throws SQLException {
    return new Genre(
        rs.getInt("id"),
        rs.getString("name")
    );
  }
}
