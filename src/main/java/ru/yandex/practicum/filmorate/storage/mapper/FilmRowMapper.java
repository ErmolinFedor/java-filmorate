package ru.yandex.practicum.filmorate.storage.mapper;

import java.time.Duration;
import java.util.LinkedHashSet;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import java.sql.ResultSet;
import java.sql.SQLException;
import ru.yandex.practicum.filmorate.model.Mpa;

@Component
public class FilmRowMapper implements RowMapper<Film> {

  @Override
  public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
    long seconds = rs.getLong("duration_seconds");

    return Film.builder()
        .id(rs.getInt("id"))
        .name(rs.getString("name"))
        .description(rs.getString("description"))
        .releaseDate(rs.getDate("release_date").toLocalDate())
        .duration(Duration.ofSeconds(seconds))
        .mpa(new Mpa(
            rs.getInt("mpa_id"),
            rs.getString("mpa_name")
        ))
        .genres(new LinkedHashSet<>())
        .build();
  }
}
