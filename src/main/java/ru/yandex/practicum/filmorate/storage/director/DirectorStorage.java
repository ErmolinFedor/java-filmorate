package ru.yandex.practicum.filmorate.storage.director;

import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DirectorStorage {

  Collection<Director> findAll();

  Director create(@RequestBody Director film);

  Director update(@RequestBody Director newFilm);

  Optional<Director> findById(int id);

  List<Director> findAllByIds(List<Integer> ids);

  void delete(int directorId);

}
