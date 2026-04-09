package ru.yandex.practicum.filmorate.storage.feed;

import java.time.Instant;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.BaseRepository;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;

@Slf4j
@Repository
public class FeedDbStorage extends BaseRepository<Event> implements FeedStorage {

  private static final String INSERT_EVENT =
      "INSERT INTO events (user_id, entity_id, event_type, operation, timestamp) " +
          "VALUES (?, ?, ?, ?, ?)";

  private static final String FIND_BY_USER_ID =
      "SELECT * FROM events WHERE user_id = ? ORDER BY timestamp ASC";

  public FeedDbStorage(JdbcTemplate jdbc, RowMapper<Event> mapper) {
    super(jdbc, mapper, Event.class);
  }

  @Override
  public void addEvent(Integer userId, Integer entityId, EventType eventType, Operation operation) {
    log.info("Регистрация события {} {} для пользователя {}", eventType, operation, userId);
    insert(INSERT_EVENT,
        userId,
        entityId,
        eventType.name(),
        operation.name(),
        Instant.now().toEpochMilli()
    );
  }

  @Override
  public Collection<Event> getFeed(Integer userId) {
    log.info("Получение ленты событий для пользователя {}", userId);
    return findAll(FIND_BY_USER_ID, userId);
  }
}
