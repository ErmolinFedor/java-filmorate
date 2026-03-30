package ru.yandex.practicum.filmorate.storage.genres;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import ru.yandex.practicum.filmorate.model.Genre;

public interface GenreStorage {

  Collection<Genre> findAll();

  Optional<Genre> findById(int id);

  List<Genre> findAllByIds(List<Integer> ids);
}
