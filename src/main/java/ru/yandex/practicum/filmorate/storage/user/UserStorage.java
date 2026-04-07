package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {

  Collection<User> findAll();

  User create(@RequestBody User user);

  User update(@RequestBody User newUser);

  Optional<User> findById(int id);

  void addFriend(int userId, int friendId);

  void removeFriend(int userId, int friendId);

  Collection<User> getFriends(int userId);

  Collection<User> getCommonFriends(int userId, int otherId);
}
