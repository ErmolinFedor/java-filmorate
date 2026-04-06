package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exeption.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genres.GenreStorage;

import java.util.Collection;

@Slf4j
@Service
public class GenreService {
  private final GenreStorage genreStorage;

  @Autowired
  public GenreService(@Qualifier("genreDbStorage") GenreStorage genreStorage) {
    this.genreStorage = genreStorage;
  }

  public Collection<Genre> findAll() {
    return genreStorage.findAll();
  }

  public Genre findById(int id) {
    return genreStorage.findById(id)
        .orElseThrow(() -> new NotFoundException("Жанр с id=" + id + " не найден"));
  }
}
