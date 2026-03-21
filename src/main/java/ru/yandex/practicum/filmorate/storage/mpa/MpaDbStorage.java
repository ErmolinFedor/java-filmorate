package ru.yandex.practicum.filmorate.storage.mpa;

import java.util.Collection;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.BaseRepository;
import ru.yandex.practicum.filmorate.model.Mpa;

@Component
@Slf4j
public class MpaDbStorage extends BaseRepository<Mpa> implements MpaStorage {
  private static final String FIND_ALL_QUERY = "SELECT * FROM mpa_ratings ORDER BY id";
  private static final String FIND_BY_ID_QUERY = "SELECT * FROM mpa_ratings WHERE id = ?";

  public MpaDbStorage(JdbcTemplate jdbc, RowMapper<Mpa> mapper) {
    super(jdbc, mapper, Mpa.class);
  }

  @Override
  public Collection<Mpa> findAll() {
    return findAll(FIND_ALL_QUERY);
  }

  @Override
  public Optional<Mpa> findById(int id) {
    return findOne(FIND_BY_ID_QUERY, id);
  }
}
