package ru.yandex.practicum.filmorate.storage.user;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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
}
