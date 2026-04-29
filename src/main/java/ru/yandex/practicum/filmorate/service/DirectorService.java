package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.exeption.NotFoundException;
import ru.yandex.practicum.filmorate.exeption.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.Collection;

@Slf4j
@Service
public class DirectorService {
  private final DirectorStorage directorStorage;

  @Autowired
  public DirectorService(@Qualifier("directorDbStorage") DirectorStorage directorStorage) {
    this.directorStorage = directorStorage;
  }

  public Collection<Director> findAll() {
    return directorStorage.findAll();
  }

  public Director create(@RequestBody Director director) throws ValidationException {
    return directorStorage.create(director);
  }

  public Director update(@RequestBody Director newDirector) throws ValidationException, NotFoundException {

    Director oldDirector = getDirectorOrThrow(newDirector.getId());
    // Обновление полей, если они не null
    if (newDirector.getName() != null) {
      oldDirector.setName(newDirector.getName());
      log.debug("обновлено поле: Name, новое значение: {}", newDirector.getName());
    }

    directorStorage.update(oldDirector);
    log.info("Обновлен фильм: {}", oldDirector.getName());
    return oldDirector;
  }

  public Director getDirectorById(int id) {
    return getDirectorOrThrow(id);
  }

  public Director remove(int id) {
    Director director = getDirectorOrThrow(id);
    directorStorage.delete(id);
    return director;
  }

  private Director getDirectorOrThrow(int id) throws NotFoundException {
    return directorStorage.findById(id).orElseThrow(() -> {
      log.warn("Не найден режиссёр: id = {}", id);
      return new NotFoundException("Режиссёра с Id " + id + " не найден");
    });
  }
}
