package ru.yandex.practicum.filmorate.storage.mpa;

import java.util.Collection;
import java.util.Optional;
import ru.yandex.practicum.filmorate.model.Mpa;

public interface MpaStorage {

  Collection<Mpa> findAll();

  Optional<Mpa> findById(int id);
}
