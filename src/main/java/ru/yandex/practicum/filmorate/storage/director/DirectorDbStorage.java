package ru.yandex.practicum.filmorate.storage.director;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.BaseRepository;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class DirectorDbStorage extends BaseRepository<Director> implements DirectorStorage {

  private static final String FIND_ALL_QUERY =
    "SELECT d.* FROM directors d";
  private static final String FIND_BY_ID_QUERY =
    "SELECT d.* FROM directors d WHERE d.id = ?";
  private static final String INSERT_QUERY =
    "INSERT INTO directors(name) VALUES (?)";
  private static final String UPDATE_QUERY =
    "UPDATE directors SET name = ? WHERE id = ?";
  private static final String DELETE_QUERY =
    "DELETE FROM directors WHERE id = ?";

  public DirectorDbStorage(JdbcTemplate jdbc, RowMapper<Director> mapper) {
    super(jdbc, mapper, Director.class);
  }

  @Override
  public Collection<Director> findAll() {
    Collection<Director> directors = findAll(FIND_ALL_QUERY);
    return directors;
  }

  @Override
  public Director create(Director director) {
    int id = insert(INSERT_QUERY,
      director.getName()
    );
    director.setId(id);
    return director;
  }

  @Override
  public Director update(Director newDirector) {
    update(UPDATE_QUERY,
      newDirector.getName(),
      newDirector.getId()
    );
    return newDirector;
  }

  @Override
  public Optional<Director> findById(int id) {
    Optional<Director> directorOpt = findOne(FIND_BY_ID_QUERY, id);
    return directorOpt;
  }

  @Override
  public List<Director> findAllByIds(List<Integer> ids) {
    if (ids.isEmpty()) {
      return Collections.emptyList();
    }

    String inSql = String.join(",", Collections.nCopies(ids.size(), "?"));
    String sql = "SELECT * FROM directors WHERE id IN (" + inSql + ")";

    return jdbc.query(sql, mapper, ids.toArray());
  }

  @Override
  public void delete(int id) {
    jdbc.update(DELETE_QUERY, id);
  }
}
