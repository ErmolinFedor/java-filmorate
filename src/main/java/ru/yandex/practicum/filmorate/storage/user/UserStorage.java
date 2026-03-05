package ru.yandex.practicum.filmorate.storage.user;

import java.util.Collection;
import java.util.Optional;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.model.User;

public interface UserStorage {

  Collection<User> findAll();

  User create(@RequestBody User user);

  User update(@RequestBody User newUser);

  Optional<User> findById(int id);
}
