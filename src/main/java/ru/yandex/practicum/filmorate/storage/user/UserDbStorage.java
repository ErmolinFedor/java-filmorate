package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.BaseRepository;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mapper.UserRowMapper;

import java.util.Collection;
import java.util.Optional;

@Component
@Slf4j
public class UserDbStorage extends BaseRepository<User> implements UserStorage {

  private static final String FIND_ALL_QUERY = "SELECT * FROM users";
  private static final String FIND_BY_ID_QUERY = "SELECT * FROM users WHERE id = ?";
  private static final String INSERT_QUERY =
      "INSERT INTO users(email, login, name, birthday) VALUES (?, ?, ?, ?)";
  private static final String UPDATE_QUERY =
      "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
  private static final String INSERT_FRIEND_QUERY =
      "INSERT INTO friends (id_user, id_friend) VALUES (?, ?)";
  private static final String DELETE_FRIEND_QUERY =
      "DELETE FROM friends WHERE id_user = ? AND id_friend = ?";
  private static final String FIND_FRIENDS_QUERY =
      "SELECT u.* FROM users u JOIN friends f ON u.id = f.id_friend " +
          "WHERE f.id_user = ?";
  private static final String FIND_COMMON_FRIENDS_QUERY =
      "SELECT u.* FROM users u " +
          "JOIN friends f1 ON u.id = f1.id_friend " +
          "JOIN friends f2 ON u.id = f2.id_friend " +
          "WHERE f1.id_user = ? AND f2.id_user = ?";

  public UserDbStorage(JdbcTemplate jdbc, UserRowMapper mapper) {
    super(jdbc, mapper, User.class);
  }

  @Override
  public Collection<User> findAll() {
    return findAll(FIND_ALL_QUERY);
  }

  @Override
  public User create(User user) {
    int id = insert(
        INSERT_QUERY,
        user.getEmail(),
        user.getLogin(),
        user.getName(),
        user.getBirthday()
    );
    user.setId(id);
    return user;
  }

  @Override
  public User update(User newUser) {
    update(
        UPDATE_QUERY,
        newUser.getEmail(),
        newUser.getLogin(),
        newUser.getName(),
        newUser.getBirthday(),
        newUser.getId()
    );
    return newUser;
  }

  @Override
  public Optional<User> findById(int id) {
    return findOne(FIND_BY_ID_QUERY, id);
  }

  @Override
  public void addFriend(int userId, int friendId) {
    jdbc.update(INSERT_FRIEND_QUERY, userId, friendId);
  }

  @Override
  public void removeFriend(int userId, int friendId) {
    jdbc.update(DELETE_FRIEND_QUERY, userId, friendId);
  }

  @Override
  public Collection<User> getFriends(int userId) {
    return jdbc.query(FIND_FRIENDS_QUERY, mapper, userId);
  }

  @Override
  public Collection<User> getCommonFriends(int userId, int otherId) {
    return jdbc.query(FIND_COMMON_FRIENDS_QUERY, mapper, userId, otherId);
  }
}
