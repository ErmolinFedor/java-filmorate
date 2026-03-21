package ru.yandex.practicum.filmorate.storage.user;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.model.User;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {
  private final Map<Integer, User> users = new HashMap<>();
  private int idCounter = 0;

  @Override
  public Collection<User> findAll() {
    return users.values();
  }

  @Override
  public User create(@RequestBody User user) {
    user.setId(++idCounter);
    users.put(user.getId(), user);
    return user;
  }

  @Override
  public User update(@RequestBody User user) {
    users.put(user.getId(), user);
    return user;
  }

  @Override
  public Optional<User> findById(int id) {
    return Optional.ofNullable(users.get(id));
  }

  @Override
  public void addFriend(int userId, int friendId) {
    User user = users.get(userId);
    User friend = users.get(friendId);
    if (user != null && friend != null) {
      user.getFriends().add(friendId);
       friend.getFriends().add(userId);
    }
  }

  @Override
  public void removeFriend(int userId, int friendId) {
    User user = users.get(userId);
    if (user != null) {
      user.getFriends().remove(friendId);
    }
  }

  @Override
  public Collection<User> getFriends(int userId) {
    User user = users.get(userId);
    if (user == null) return Collections.emptyList();

    return user.getFriends().stream()
        .map(users::get)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  @Override
  public Collection<User> getCommonFriends(int userId, int otherId) {
    User user = users.get(userId);
    User other = users.get(otherId);

    if (user == null || other == null) return Collections.emptyList();

    Set<Integer> userFriends = user.getFriends();
    Set<Integer> otherFriends = other.getFriends();

    return userFriends.stream()
        .filter(otherFriends::contains)
        .map(users::get)
        .collect(Collectors.toList());
  }
}
